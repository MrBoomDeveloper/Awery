package com.mrboomdev.awery.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.mrboomdev.awery.ext.util.Image

expect class PlatformImage: Image {
	@Composable
	fun rememberPainter(): Painter
}