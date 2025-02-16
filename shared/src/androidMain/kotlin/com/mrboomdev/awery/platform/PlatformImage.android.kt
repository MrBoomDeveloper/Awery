package com.mrboomdev.awery.platform

import android.graphics.drawable.Drawable
import coil3.compose.asPainter
import coil3.asImage as asCoilImage

fun Drawable.asImage() = PlatformImage(asCoilImage().asPainter(Platform))