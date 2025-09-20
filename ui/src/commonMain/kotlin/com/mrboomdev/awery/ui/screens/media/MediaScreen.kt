package com.mrboomdev.awery.ui.screens.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.CacheStorage
import com.mrboomdev.awery.core.utils.LoadingStatus
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.launchTryingSupervise
import com.mrboomdev.awery.core.utils.mayStartLoading
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.toDBMedia
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.loaders.watch.ExtensionsWatcher
import com.mrboomdev.awery.extension.loaders.watch.WatcherNode
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.chaps
import com.mrboomdev.awery.resources.comments
import com.mrboomdev.awery.resources.episodes
import com.mrboomdev.awery.resources.info
import com.mrboomdev.awery.resources.read
import com.mrboomdev.awery.resources.relations
import com.mrboomdev.awery.resources.watch
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.utils.ColorSerializer
import com.mrboomdev.awery.ui.utils.viewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.resolve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

enum class MediaScreenTabs {
    INFO {
        @Composable
        override fun getTitle(media: Media): String {
            return stringResource(Res.string.info)
        }

        override fun isVisible(media: Media): Boolean {
            return media.description != null || media.tags?.isNotEmpty() == true
        }
    },

    EPISODES {
        @Composable
        override fun getTitle(media: Media): String {
            return stringResource(when(media.type) {
                Media.Type.WATCHABLE -> Res.string.watch
                Media.Type.READABLE -> Res.string.read
            })
        }
        
        override fun isVisible(media: Media): Boolean {
            return true
        }
    },

    COMMENTS {
        @Composable
        override fun getTitle(media: Media): String {
            return stringResource(Res.string.comments)
        }
        
        override fun isVisible(media: Media): Boolean {
            return false // This tab isn't done yet!
        }
    },
    
    RELATIONS {
        @Composable
        override fun getTitle(media: Media): String {
            return stringResource(Res.string.relations)
        }
        
        override fun isVisible(media: Media): Boolean {
            return false // This tab isn't done yet!
        }
    };

    @Composable
    abstract fun getTitle(media: Media): String
    
    abstract fun isVisible(media: Media): Boolean
}

@Composable
expect fun MediaScreen(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel = viewModel { MediaScreenViewModel(destination) }
)

@Serializable
internal data class ColorScheme(
    @Serializable(with = ColorSerializer::class) val color: Color,
    @Serializable(with = ColorSerializer::class) val onColor: Color
)

internal val colorsCache = runBlocking {
    CacheStorage<ColorScheme>(
        directory = FileKit.cacheDir.resolve("colorSchemes"),
        maxSize = 5 * 1024 * 1024
    )
}

class MediaScreenViewModel(
    private val destination: Routes.Media
): ViewModel() {
    private val _watcher = mutableStateOf<WatcherNode.Variants>(
        object : WatcherNode.Variants {
            override val children = emptyList<WatcherNode>()
            override suspend fun load() {}
            override val title = "Loading..."
            override val id = "loading"
        }
    )
    val watcher by _watcher
    
    private val _isUpdatingMedia = MutableStateFlow(true)
    val isUpdatingMedia = _isUpdatingMedia.asStateFlow()
    
    private val _episodesLoadingStatus = mutableStateOf<LoadingStatus>(LoadingStatus.NotInitialized)
    val episodesLoadingStatus by _episodesLoadingStatus
    
    private val _media = mutableStateOf(destination.media)
    val media by _media
    
    init {
        viewModelScope.launchTryingSupervise(Dispatchers.Default, onCatch = {
            _isUpdatingMedia.value = false
            Log.e("MediaScreen", "Failed to update media!", it)
            loadEpisodes()
        }) {
            val catalogModule = Extensions[destination.extensionId]!!.get<CatalogModule>()
            
            if(catalogModule != null) {
                _media.value = catalogModule.updateMedia(destination.media).also { updated ->
                    Awery.database.media.update(updated.toDBMedia(destination.extensionId))
                }
            }
            
            _isUpdatingMedia.emit(false)
            loadEpisodes()
        }
    }
    
    fun loadEpisodes() {
        if(!episodesLoadingStatus.mayStartLoading) return
        _episodesLoadingStatus.value = LoadingStatus.Loading
        
        viewModelScope.launch(Dispatchers.Default) {
            _watcher.value = ExtensionsWatcher(destination.extensionId, media, viewModelScope)
            watcher.load()
        }
    }
}