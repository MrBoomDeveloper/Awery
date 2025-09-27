package com.mrboomdev.awery.ui.screens.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.CacheStorage
import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.core.utils.collection.MutableStateListFlow
import com.mrboomdev.awery.core.utils.collection.minusAssign
import com.mrboomdev.awery.core.utils.collection.plusAssign
import com.mrboomdev.awery.core.utils.collection.replace
import com.mrboomdev.awery.core.utils.launchTryingSupervise
import com.mrboomdev.awery.data.AgeRating
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.toMedia
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.lang.System.currentTimeMillis
import kotlin.collections.emptyList
import kotlin.collections.filter
import kotlin.collections.filterNotNull
import kotlin.collections.first
import kotlin.collections.map
import kotlin.collections.mutableListOf
import kotlin.collections.plusAssign
import kotlin.collections.zip

private val feedsCache by lazy {
    runBlocking {
        CacheStorage<Results<Media>>(
            directory = FileKit.cacheDir / "feeds",
            maxSize = 5 * 1024 * 1024 * 8, // 5 mb
            maxAge = 24 * 60 * 60 * 1000 // 1 day
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModel(savedStateHandle: SavedStateHandle): ViewModel() {
    private var receiveResultsAfter = currentTimeMillis()
    private var job: Job? = null
    
    private val _loadedFeeds = MutableStateListFlow<Triple<Extension, Feed, Results<Media>>>()
    val loadedFeeds = _loadedFeeds.asStateFlow()

    private val _failedFeeds = MutableStateListFlow<Triple<Extension, Feed, Throwable>>()
    val failedFeeds = _failedFeeds.asStateFlow()
    
    private val _isLoadingFeeds = MutableStateFlow(true)
    val isLoadingFeeds = _isLoadingFeeds.asStateFlow()

    private val _isReloadingFeeds = MutableStateFlow(true)
    val isReloadingFeeds = _isReloadingFeeds.asStateFlow()
    
    val isNoLists = Awery.database.lists.observeCount()
        .map { it == 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    
    val lists = Awery.database.lists.observeAll()
        .flatMapLatest { lists ->
            combine(lists.map { list ->
                Awery.database.lists.observeMediaInList(list.id)
            }) { lists.zip(it) { list, mediaList ->
                list to mediaList.map { media ->
                    media to media.toMedia()
                }
            } }
        }
        .map { true to it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false to emptyList()
        )

    val continueWatching = Awery.database.progress
        .observeLatest(25)
        .map { all ->
            all.map { watchProgress ->
				val media = Awery.database.media.get(
                    extensionId = watchProgress.extensionId, 
                    id = watchProgress.mediaId
                ) ?: return@map null

				watchProgress to media.toMedia()
            }.filterNotNull()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    
    init {
        loadFeeds(useCache = true)
    }
    
    fun reloadFeeds() {
        receiveResultsAfter = currentTimeMillis()
        
        runBlocking { 
            _isReloadingFeeds.emit(true)
        }
        
        loadFeeds(useCache = false)
    }

    private fun loadFeeds(useCache: Boolean) {
        val startTime = currentTimeMillis()
        _isLoadingFeeds.value = true
        job?.cancel()
        
        suspend fun cleanupStuffIfNeeded() {
            if(_isReloadingFeeds.value) {
                _loadedFeeds.emit(emptyList())
                _failedFeeds.clear()
                _isReloadingFeeds.emit(false)
            }
        }
        
        job = viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
        }) {
            _loadedFeeds.clear()
            
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
                                    mutableListOf<String>() += ""
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
                            
                            if(startTime >= receiveResultsAfter) {
                                cleanupStuffIfNeeded()
                                _loadedFeeds += Triple(extension, feed, loadedMedia)
								feedsCache["${extension.id}:${feed.id}"] = loadedMedia
                            }
                        } catch(t: Throwable) {
                            if(startTime >= receiveResultsAfter) {
                                cleanupStuffIfNeeded()
                                _failedFeeds += Triple(extension, feed, t)
                            }
                        }
                    }
                }.collect()
            
            _isReloadingFeeds.emit(false)
            _isLoadingFeeds.value = false
        }
    }

    /**
     * @param onResult feedIndex is null if failed to reload a feed.
     */
    fun reloadFeed(
        extension: Extension,
        feed: Feed,
        onResult: (feedIndex: Int?) -> Unit
    ) {
        val key = _failedFeeds.first { it.first == extension && it.second == feed }
        
        viewModelScope.launchTryingSupervise(Dispatchers.Default, onCatch = {
            runBlocking {
                _failedFeeds.replace(key, Triple(extension, feed, it))
            }
            
            onResult(null)
        }) {
            val results = extension.get<CatalogModule>()!!.loadFeed(feed).apply {
                if(items.isEmpty()) throw NothingFoundException("Feed loaded with 0 results.")
            }
            
            val index = _loadedFeeds.size
            _loadedFeeds.add(index, Triple(extension, feed, results))
            _failedFeeds -= key
            onResult(index)
        }
    }

    fun loadMoreFeeds() {
        // TODO: If more extensions will be loaded during the runtime then lazily load their feeds here.
        println("Load more feeds")
    }
}