package com.mrboomdev.awery.ui.components

import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay

@Composable
actual fun VideoPlayer(
	modifier: Modifier, 
	url: String,
	state: VideoPlayerState
) {
	val context = LocalContext.current
	
	val player = remember {
		val listener = object : Player.Listener {
			override fun onIsLoadingChanged(isLoading: Boolean) {
				state.isBuffering = isLoading
			}
			
			override fun onIsPlayingChanged(isPlaying: Boolean) {
				state.isPaused = !isPlaying
			}
		}
		
		val audioAttributes = AudioAttributes.Builder()
			.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
			.setUsage(C.USAGE_MEDIA)
			.build()
		
		ExoPlayer.Builder(context)
			.setAudioAttributes(audioAttributes, true)
			.build().apply {
				setHandleAudioBecomingNoisy(true)
				addListener(listener)
			}
	}
	
	LaunchedEffect(url, state) { 
		while(true) {
			delay(1000L)
			state.currentTime = player.currentPosition.let {
				if(it < 0) 0 else it // Weird bugs with ExoPlayer playback
			}
		}
	}
	
	AndroidView(
		modifier = modifier,
		factory = {
			SurfaceView(context).apply {
				player.setVideoSurfaceView(this)
			}
		}
	)
}