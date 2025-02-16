package com.mrboomdev.awery.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.mrboomdev.awery.ext.util.Image

class PlatformImage(private val painter: Painter): Image() {
	@Composable
	operator fun invoke(
		modifier: Modifier = Modifier,
		alignment: Alignment = Alignment.Center,
		contentScale: ContentScale = ContentScale.Fit,
		alpha: Float = 1F
	) {
		Image(
			painter = painter,
			contentDescription = null,
			alignment = alignment,
			contentScale = contentScale,
			alpha = alpha
		)
	}
}