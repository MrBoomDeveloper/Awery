package com.mrboomdev.awery.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.mrboomdev.awery.ext.util.Image

actual class PlatformImage : Image {
	@Composable
	actual fun rememberPainter(): Painter {
		TODO("Not yet implemented")
	}
}