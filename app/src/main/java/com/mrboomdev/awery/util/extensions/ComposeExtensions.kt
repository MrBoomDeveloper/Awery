package com.mrboomdev.awery.util.extensions

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay

suspend fun MutableInteractionSource.performClick(duration: Long = 0) {
	val press = PressInteraction.Press(Offset.Zero)
	emit(press)
	delay(duration)
	emit(PressInteraction.Release(press))
}