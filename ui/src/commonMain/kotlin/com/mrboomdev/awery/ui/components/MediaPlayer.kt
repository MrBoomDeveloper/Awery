package com.mrboomdev.awery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun MediaPlayer(
    state: MediaPlayerState,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
)

expect class MediaPlayerState internal constructor(
    autoDispose: Boolean,
    onEnd: () -> Unit,
    onError: (Exception) -> Unit
) {
    val url: String?
    val position: Long
    val bufferedPosition: Long
    val duration: Long
    val isPaused: Boolean
    val isLoading: Boolean
    val aspectRatio: Pair<Int, Int>
    val didRestoreState: Boolean
    fun setUrl(url: String?)
    fun seekTo(position: Long)
    fun pause()
    fun play()
    fun dispose()
    internal fun saveState(state: MutableMap<String, Any?>)
    internal fun restoreState(state: Map<String, Any?>)
    companion object
}

fun MediaPlayerState.Companion.Saver(
    factory: () -> MediaPlayerState
) = mapSaver(
    save = { instance ->
        buildMap { instance.saveState(this) }
    },

    restore = { saved ->
        factory().apply { restoreState(saved) }
    }
)

@Composable
fun rememberPlayerState(
    autoDispose: Boolean = true,
    onEnd: () -> Unit = {},
    onError: (Exception) -> Unit = { throw it }
): MediaPlayerState {
    return rememberSaveable(saver = MediaPlayerState.Saver {
        MediaPlayerState(autoDispose, onEnd, onError)
    }) {
        MediaPlayerState(autoDispose, onEnd, onError)
    }
}