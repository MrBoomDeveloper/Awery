package com.mrboomdev.awery.ui.screens.search

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.core.utils.collection.iterateMutable
import com.mrboomdev.awery.core.utils.collection.replace
import com.mrboomdev.awery.core.utils.launchSupervise
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.core.utils.launchTryingSupervise
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.loaders.Extensions.has
import com.mrboomdev.awery.extension.sdk.*
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.ui.App
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
	private val jobs = mutableListOf<Job>()
	
	private val _isLoadingFeeds = MutableStateFlow(false)
	val isLoadingFeeds = _isLoadingFeeds.asStateFlow()
	
	private val _loadedFeeds = mutableStateListOf<Triple<Extension, Results<Media>, List<Preference<*>>>>()
	val loadedFeeds: List<Triple<Extension, Results<Media>, List<Preference<*>>>> = _loadedFeeds

	private val _failedFeeds = mutableStateListOf<Triple<Extension, Throwable, List<Preference<*>>>>()
	val failedFeeds: List<Triple<Extension, Throwable, List<Preference<*>>>> = _failedFeeds
	
	private val _extensionsFound = combine(
		App.searchQuery,
		Extensions.observeAll(enabled = true),
		AwerySettings.adultContent.stateFlow
	) { query, extensions, adultContentMode ->
		extensions.filter { extension ->
			extension.name.lowercase().contains(query.lowercase()) && extension.has<CatalogModule>() && when(adultContentMode) {
				AwerySettings.AdultContent.SHOW -> true
				AwerySettings.AdultContent.HIDE -> !extension.isNsfw
				AwerySettings.AdultContent.ONLY -> extension.isNsfw
			}
		}
	}
	
	val extensionsFound = _extensionsFound.map { extensions ->
		extensions.sortedBy { extension ->
			extension.name.lowercase()
		}
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = null
	)
	
	init {
		viewModelScope.launchSupervise(Dispatchers.Default) {
			combine(
				App.searchQuery,
				Extensions.observeAll(enabled = true),
				AwerySettings.adultContent.stateFlow
			) { query, extensions, adultContentMode ->
				query to extensions.filter { extension ->
					extension.has<CatalogModule>() && when(adultContentMode) {
						AwerySettings.AdultContent.SHOW -> true
						AwerySettings.AdultContent.HIDE -> !extension.isNsfw
						AwerySettings.AdultContent.ONLY -> extension.isNsfw
					}
				}
			}.onEach { (query, extensions) ->
				jobs.iterateMutable { 
					it.cancel()
					remove()
				}
				
				_failedFeeds.clear()
				_loadedFeeds.clear()
				_isLoadingFeeds.emit(query.isNotBlank())

				if(query.isBlank()) {
					// We don't want to search for anything because user don't want to see unwanted stuff here.
					return@onEach
				}
				
				var doneJobs = 0
				
				suspend fun checkIfDoneAllJobs() {
					if(doneJobs < extensions.size) return
					_isLoadingFeeds.emit(false)
				}
				
				extensions.forEach { extension ->
					extension.get<CatalogModule>()?.also { catalogModule ->
						val filters = catalogModule.getDefaultFilters()

						filters.find {
							it.role == Preference.Role.QUERY
						}?.let { it as? StringPreference }?.apply {
							value = query.trim()
						}
						
						jobs += launchTryingSupervise(Dispatchers.Default, onCatch = {
							jobs += launch { 
								_failedFeeds += Triple(extension, it, filters)
								doneJobs++
								checkIfDoneAllJobs()
							}
						}) {
							_loadedFeeds += Triple(extension, catalogModule.search(filters).also { 
								if(it.items.isEmpty()) {
									throw NothingFoundException()
								}
							}, filters)
							
							doneJobs++
							checkIfDoneAllJobs()
						}
					}
				}
			}.conflate().collect()
		}
	}

	fun reloadFeed(
		extension: Extension,
		filters: List<Preference<*>>,
		onResult: (feedIndex: Int?) -> Unit
	) {
		val key = _failedFeeds.first { it.first == extension }

		viewModelScope.launchTrying(Dispatchers.Default, onCatch = {
			if(it is CancellationException) return@launchTrying
			_failedFeeds.replace(key, Triple(extension, it, filters))
			onResult(null)
		}) {
			val results = extension.get<CatalogModule>()!!.search(filters).apply {
				if(items.isEmpty()) throw NothingFoundException("Feed loaded with 0 results.")
			}

			val index = _loadedFeeds.size
			_loadedFeeds.add(index, Triple(extension, results, filters))
			_failedFeeds -= key
			onResult(index)
		}.apply {
			jobs += this

			invokeOnCompletion {
				viewModelScope.launch {
					jobs.remove(this@apply)
				}
			}
		}
	}
}