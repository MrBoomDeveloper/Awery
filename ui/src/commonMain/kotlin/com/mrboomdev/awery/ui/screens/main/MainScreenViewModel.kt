package com.mrboomdev.awery.ui.screens.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.CacheStorage
import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.core.utils.launchTryingSupervise
import com.mrboomdev.awery.core.utils.replace
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.lang.System.currentTimeMillis

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
    
    var isReloading by mutableStateOf(false)
        private set
    
    val loadedFeeds = mutableStateListOf<Triple<Extension, Feed, Results<Media>>>()
    val failedFeeds = mutableStateListOf<Triple<Extension, Feed, Throwable>>()
    
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
                val media = Awery.database.media.get(watchProgress.extensionId, watchProgress.mediaId)

                if(media == null) {
                    return@map null
                }

                watchProgress to media.toMedia()
            }.filterNotNull()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _isLoadingFeeds = mutableStateOf(true)
    val isLoadingFeeds by _isLoadingFeeds
    
    init {
        loadFeeds(useCache = true)
    }
    
    fun reloadFeeds() {
        receiveResultsAfter = currentTimeMillis()
        isReloading = true
        loadFeeds(useCache = false)
    }

    private fun loadFeeds(useCache: Boolean) {
        val startTime = currentTimeMillis()
        _isLoadingFeeds.value = true
        job?.cancel()
        
        fun cleanupStuffIfNeeded() {
            if(isReloading) {
                loadedFeeds.clear()
                failedFeeds.clear()
                isReloading = false
            }
        }
        
        job = viewModelScope.launch(Dispatchers.Default) {
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
                                    loadedFeeds += Triple(extension, feed, it)
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
                                loadedFeeds += Triple(extension, feed, loadedMedia)
								feedsCache["${extension.id}:${feed.id}"] = loadedMedia
                            }
                        } catch(t: Throwable) {
                            if(startTime >= receiveResultsAfter) {
                                cleanupStuffIfNeeded()
                                failedFeeds += Triple(extension, feed, t)
                            }
                        }
                    }
                }.collect()

            isReloading = false
            _isLoadingFeeds.value = false
        }
    }

    /**
     * @param onResult feedIndex is null if failed to reload an feed.
     */
    fun reloadFeed(
        extension: Extension,
        feed: Feed,
        onResult: (feedIndex: Int?) -> Unit
    ) {
        val key = failedFeeds.first { it.first == extension && it.second == feed }
        
        viewModelScope.launchTryingSupervise(Dispatchers.Default, onCatch = {
            failedFeeds.replace(key, Triple(extension, feed, it))
            onResult(null)
        }) {
            val results = extension.get<CatalogModule>()!!.loadFeed(feed).apply {
                if(items.isEmpty()) throw NothingFoundException("Feed loaded with 0 results.")
            }
            
            val index = loadedFeeds.size
            loadedFeeds.add(index, Triple(extension, feed, results))
            failedFeeds -= key
            onResult(index)
        }
    }

    fun loadMoreFeeds() {
        // TODO: If more extensions will be loaded during the runtime then lazily load their feeds here.
        println("Load more feeds")
    }
}