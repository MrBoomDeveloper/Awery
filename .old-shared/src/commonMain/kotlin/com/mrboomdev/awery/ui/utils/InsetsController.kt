package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf

class InsetsController {
	val stack = mutableStateListOf(InsetsControllerItem(InsetsVisibility.SHOWN))
}

class InsetsControllerItem(
	val visibility: InsetsVisibility? = null
)

val LocalInsetsController = compositionLocalOf<InsetsController> { 
	throw NotImplementedError("No InsetsController was passed!")
}

enum class InsetsVisibility {
	HIDDEN,
	TRANSPARENT,
	SHOWN
}

/**
 * Controls whatever insets should be visible or not. Only the latest call is been using
 */
@Composable
fun ControlInsets(visibility: InsetsVisibility) {
	val controller = LocalInsetsController.current
	
	DisposableEffect(visibility) {
		val item = InsetsControllerItem(visibility)
		controller.stack += item
		
		onDispose { 
			controller.stack -= item
		}
	}
}