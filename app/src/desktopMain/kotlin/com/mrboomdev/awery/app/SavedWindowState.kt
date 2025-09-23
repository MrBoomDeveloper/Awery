package com.mrboomdev.awery.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowPosition.PlatformDefault.y
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.serialization.Serializable
import java.awt.Window
import kotlin.math.max

@Serializable
data class SavedWindowState(
	internal val maximized: Boolean,
	internal val width: Float,
	internal val height: Float,
	internal val x: Float,
	internal val y: Float
)

fun WindowState.saveState(): SavedWindowState {
	return SavedWindowState(
		maximized = when(placement) {
			WindowPlacement.Maximized,
			WindowPlacement.Fullscreen -> true
			else -> false
		},

		width = size.width.value,
		height = size.height.value,
		
		x = if(!position.isSpecified || placement == WindowPlacement.Floating) {
			position.x.value
		} else Float.MAX_VALUE,
		
		y = if(!position.isSpecified || placement == WindowPlacement.Floating) {
			position.y.value
		} else Float.MAX_VALUE
	)
}

fun SavedWindowState.restoreState(): WindowState {
	return WindowState(
		width = Dp(width),
		height = Dp(height),
		
		position = if(x == Float.MAX_VALUE && y == Float.MAX_VALUE) {
			WindowPosition.PlatformDefault
		} else WindowPosition.Absolute(Dp(x), Dp(y)),
		
		placement = if(maximized) {
			WindowPlacement.Maximized
		} else WindowPlacement.Floating
	)
}