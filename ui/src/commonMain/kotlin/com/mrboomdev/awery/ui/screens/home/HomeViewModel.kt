package com.mrboomdev.awery.ui.screens.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.utils.CacheStorage
import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.core.utils.collection.iterateMutable
import com.mrboomdev.awery.core.utils.collection.replace
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.data.AgeRating
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.Feed
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Results
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private val feedsCache by lazy {
	runBlocking {
		CacheStorage<Results<Media>>(
			directory = FileKit.cacheDir / "feeds",
			maxSize = 5 * 1024 * 1024 * 8, // 5 mb
			maxAge = 24 * 60 * 60 * 1000 // 1 day
		)
	}
}

class HomeViewModel: ViewModel() {
	private val reloadJobs = mutableListOf<Job>()
	private var mainJob: Job? = null
	
	private val _isLoading = MutableStateFlow(true)
	val isLoading = _isLoading.asStateFlow()
	
	private val _isReloading = MutableStateFlow(false)
	val isReloading = _isReloading.asStateFlow()

	private val _loadedFeeds = mutableStateListOf<Triple<Extension, Feed, Results<Media>>>()
	val loadedFeeds: List<Triple<Extension, Feed, Results<Media>>> = _loadedFeeds

	private val _failedFeeds = mutableStateListOf<Triple<Extension, Feed, Throwable>>()
	val failedFeeds: List<Triple<Extension, Feed, Throwable>> = _failedFeeds
	
	init {
		mainJob = viewModelScope.launch(Dispatchers.Main) {
			// Fix initial lag on app launch
			delay(100)
			
			load(useCache = true)
		}
	}
	
	fun reload() {
		mainJob?.cancel()
		
		reloadJobs.iterateMutable { 
			it.cancel()
			remove()
		}

		mainJob = viewModelScope.launch(Dispatchers.Default) {
			_isReloading.emit(true)
			load(useCache = false)
		}
	}
	
	fun reloadFeed(
		extension: Extension, 
		feed: Feed,
		onResult: (feedIndex: Int?) -> Unit
	) {
		val key = _failedFeeds.first { it.first == extension && it.second == feed }
		
		viewModelScope.launchTrying(Dispatchers.Default, onCatch = {
			if(it is CancellationException) return@launchTrying
			_failedFeeds.replace(key, Triple(extension, feed, it))
			onResult(null)
		}) {
			val results = extension.get<CatalogModule>()!!.loadFeed(feed).apply {
				if(items.isEmpty()) throw NothingFoundException("Feed loaded with 0 results.")
			}

			val index = _loadedFeeds.size
			_loadedFeeds.add(index, Triple(extension, feed, results))
			_failedFeeds -= key
			onResult(index)
		}.apply { 
			reloadJobs += this

			invokeOnCompletion {
				viewModelScope.launch {
					reloadJobs.remove(this@apply)
				}
			}
		}
	}
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private suspend fun load(useCache: Boolean) {
		_isLoading.emit(true)
		var didCleanup = false
		
		fun cleanupStuffIfNeeded() {
			if(didCleanup) return
			didCleanup = true
			
			_loadedFeeds.clear()
			_failedFeeds.clear()
		}
		
		Extensions.getAll<CatalogModule>(enabled = true)
			.filter { extension ->
				when(AwerySettings.adultContent.value) {
					AwerySettings.AdultContent.SHOW -> true
					AwerySettings.AdultContent.HIDE -> !extension.isNsfw
					AwerySettings.AdultContent.ONLY -> extension.isNsfw
				}
			}.map { extension ->
				extension.get<CatalogModule>()!!.let { feedsModule ->
					Triple(extension, feedsModule, feedsModule.getFeeds(0).items)
				}
			}.flatMapMerge { (extension, feedsModule, feeds) ->
				feeds.asFlow().map { feed ->
					try {
						if(useCache) {
							feedsCache["${extension.id}:${feed.id}"]?.also {
								cleanupStuffIfNeeded()
								_loadedFeeds += Triple(extension, feed, it)
								return@map
							}
						}

						val loadedMedia = feedsModule.loadFeed(feed).let { og ->
							og.copy(items = og.items.filter { media ->
								when(AwerySettings.adultContent.value) {
									AwerySettings.AdultContent.SHOW -> true

									AwerySettings.AdultContent.HIDE ->
										media.ageRating?.let { AgeRating.of(it) } != AgeRating.NSFW

									AwerySettings.AdultContent.ONLY ->
										media.ageRating?.let { AgeRating.of(it) } == AgeRating.NSFW
								}
							})
						}.apply {
							if(items.isEmpty()) throw NothingFoundException("Feed loaded with 0 results.")
						}

						cleanupStuffIfNeeded()
						_loadedFeeds += Triple(extension, feed, loadedMedia)
						feedsCache["${extension.id}:${feed.id}"] = loadedMedia
					} catch(t: Throwable) {
						cleanupStuffIfNeeded()
						_failedFeeds += Triple(extension, feed, t)
					}
				}
			}.collect()
		
		_isReloading.emit(false)
		_isLoading.emit(false)
	}
}