package com.mrboomdev.awery.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun MediaPlayer(
    state: MediaPlayerState,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var wasPausedByUser by rememberSaveable { mutableStateOf(false) }

    LifecycleStartEffect(state) {
        if(state.autoDispose) {
            state.init(context)
        } else {
            if(state.didInit) {
                if(!wasPausedByUser) {
                    state.play()
                }
            } else {
                state.init(context)
            }
        }

        coroutineScope.launch(Dispatchers.Default) { state.observePosition() }

        onStopOrDispose {
            wasPausedByUser = state.isPaused

            if(state.autoDispose) {
                state.dispose()
            } else {
                state.pause()
            }
        }
    }

    state.player?.also { player ->
        val presentationState = rememberPresentationState(player)
        val scaledModifier = Modifier.resizeWithContentScale(contentScale, presentationState.videoSizeDp)

        Box(modifier) {
            PlayerSurface(
                modifier = scaledModifier,
                player = player,
                surfaceType = SURFACE_TYPE_SURFACE_VIEW
            )

            if(presentationState.coverSurface) {
                Box(Modifier.matchParentSize().background(Color.Black))
            }
        }
    }
}

actual class MediaPlayerState actual constructor(
    internal val autoDispose: Boolean,
    private val onEnd: () -> Unit,
    private val onError: (Exception) -> Unit
): Player.Listener {
    private val restoredState = mutableMapOf<String, Any?>()
    internal var player by mutableStateOf<Player?>(null)
    private var mediaSession: MediaSession? = null
    internal var didInit = false

    private val _url = mutableStateOf<String?>(null)
    actual val url by _url

    private val _isLoading = mutableStateOf(false)
    actual val isLoading by _isLoading
    
    private val _isPaused = mutableStateOf(false)
    actual val isPaused by _isPaused
    
    private val _position = mutableStateOf(0L)
    actual val position by _position
    
    private val _bufferedPosition = mutableStateOf(0L)
    actual val bufferedPosition by _bufferedPosition
    
    private val _duration = mutableStateOf(0L)
    actual val duration by _duration
    
    private val _aspectRatio = mutableStateOf(16 to 9)
    actual val aspectRatio by _aspectRatio
    
    private var _didRestoreState = false
    actual val didRestoreState get() = _didRestoreState

    internal fun init(context: Context) {
        dispose()
        didInit = true

        player = ExoPlayer.Builder(context).apply {
            setHandleAudioBecomingNoisy(true)
            setAudioAttributes(AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build(), true)
        }.build().apply {
            addListener(this@MediaPlayerState)
            mediaSession = MediaSession.Builder(context, this).build()
        }

        if(restoredState.isNotEmpty()) {
            setUrl(restoredState["url"] as String?)
            seekTo(restoredState["position"] as Long)

            if(restoredState["isPaused"] as Boolean) {
                _isPaused.value = true
            } else {
                _isPaused.value = false
                play()
            }

            restoredState.clear()
        }
    }

    actual fun setUrl(url: String?) {
        _url.value = url

        if(url == null) {
            player?.clearMediaItems()
        } else {
            player!!.setMediaItem(MediaItem.fromUri(url))
            player!!.prepare()
        }
    }

    actual fun play() {
        player!!.play()
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun seekTo(position: Long) {
        player!!.seekTo(position)
        _position.value = position
    }

    actual fun dispose() {
        didInit = false

        player?.removeListener(this)
        player?.release()
        player = null

        mediaSession?.release()
        mediaSession = null
    }

    internal suspend fun observePosition() {
        while(player != null) {
            delay(1000L)

            withContext(Dispatchers.Main) {
                _position.value = player?.currentPosition ?: 0L
                _bufferedPosition.value = player?.bufferedPosition ?: 0L
            }
        }
    }

    internal actual fun saveState(state: MutableMap<String, Any?>) {
        state["url"] = _url.value
        state["position"] = _position.value
        state["isPaused"] = _isPaused.value
    }

    internal actual fun restoreState(state: Map<String, Any?>) {
        _didRestoreState = true
        restoredState.putAll(state)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPaused.value = !isPlaying
    }

    override fun onPlayerError(error: PlaybackException) = onError(error)

    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState) {
            Player.STATE_READY -> {
                _isLoading.value = false
                _duration.value = player!!.duration

                _aspectRatio.value = player!!.videoSize.let {
                    if(it == VideoSize.UNKNOWN) {
                        return@let 16 to 9
                    }

                    it.width to it.height
                }
            }

            Player.STATE_BUFFERING -> {
                _isLoading.value = true
            }

            Player.STATE_ENDED -> {
                _isPaused.value = true
                pause()
                onEnd()
            }
        }
    }
    
    actual companion object
}