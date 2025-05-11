package com.mrboomdev.awery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
	modifier: Modifier = Modifier,
	url: String,
	state: VideoPlayerState = rememberVideoPlayerState()
)

@Composable
fun rememberVideoPlayerState(
	initialTime: Long = 0L
) = remember { VideoPlayerState(initialTime) }

class VideoPlayerState internal constructor(
	initialTime: Long
) {
	var isPaused by mutableStateOf(false)
	var isBuffering by mutableStateOf(true)
	var currentTime by mutableLongStateOf(initialTime)
}