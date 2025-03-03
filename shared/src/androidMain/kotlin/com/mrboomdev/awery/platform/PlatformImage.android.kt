package com.mrboomdev.awery.platform

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toDrawable
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.mrboomdev.awery.ext.util.Image

actual class PlatformImage private constructor(
	private val drawable: Drawable?,
	private val bitmap: Bitmap?
): Image {
	constructor(drawable: Drawable): this(drawable, null)
	
	@Composable
	actual fun rememberPainter(): Painter {
		if(drawable != null) {
			return rememberDrawablePainter(drawable)
		}
		
		if(bitmap != null) {
			val context = LocalContext.current
			return rememberDrawablePainter(bitmap.toDrawable(context.resources))
		}
		
		throw UnsupportedOperationException("Cannot create an painter!")
	}
}