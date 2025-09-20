package com.mrboomdev.awery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun MediaPlayer(
    state: MediaPlayerState,
    modifier: Modifier,
    contentScale: ContentScale
) {
    TODO("Desktop MediaPlayer isn't implemented yet!")
}

actual class MediaPlayerState actual constructor(
    autoDispose: Boolean,
    private val onEnd: () -> Unit,
    private val onError: (Exception) -> Unit
) {
    actual val url: String?
        get() = TODO("Not yet implemented")

    actual val position: Long
        get() = TODO("Not yet implemented")

    actual val isLoading: Boolean
        get() = true

    actual val isPaused: Boolean
        get() = TODO("Not yet implemented")

    actual fun play() {
        TODO("Not yet implemented")
    }

    actual fun setUrl(url: String?) {
    }

    actual fun seekTo(position: Long) {
    }

    actual fun pause() {
    }

    actual val duration: Long
        get() = TODO("Not yet implemented")

    actual val aspectRatio: Pair<Int, Int>
        get() = TODO("Not yet implemented")

    internal actual fun saveState(state: MutableMap<String, Any?>) {

    }

    internal actual fun restoreState(state: Map<String, Any?>) {

    }

    actual val didRestoreState: Boolean
        get() = TODO("Not yet implemented")

    actual fun dispose() {
    }

    actual companion object {}

    actual val bufferedPosition: Long
        get() = TODO("Not yet implemented")
}