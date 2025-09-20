package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.onFocusedBoundsChanged
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced

private enum class StepBeforeSaveFocus {
	GLOBAL,
	BOUNDS_TRUE,
	BOUNDS_NULL,
	FOCUS_FALSE
}

/**
 * A hacky way of restoring original focus after changing navigation destination.
 */
@Composable
fun Modifier.saveFocus(): Modifier {
	var requestFocus by rememberSaveable { mutableStateOf(false) }
	var currentStep by remember { mutableStateOf<StepBeforeSaveFocus?>(null) }
	var isFocused by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

	LaunchedEffect(Unit) {
		if(requestFocus) {
			focusRequester.requestFocus()
			requestFocus = false
		}
	}
	
	DisposableEffect(Unit) {
		onDispose {
			if(currentStep == StepBeforeSaveFocus.FOCUS_FALSE) {
				requestFocus = true
			}
		}
	}
	
	return focusRequester(focusRequester)
		.onGloballyPositioned {
			currentStep = StepBeforeSaveFocus.GLOBAL
		}
		.onFocusedBoundsChanged {
			when(currentStep) {
				StepBeforeSaveFocus.GLOBAL -> {
					currentStep = if(it?.isAttached == true) {
						 StepBeforeSaveFocus.BOUNDS_TRUE
					} else null
				}
				
				StepBeforeSaveFocus.BOUNDS_TRUE -> {
					currentStep = if(it?.isAttached == null) {
						StepBeforeSaveFocus.BOUNDS_NULL
					} else null
				}
				
				else -> {
					currentStep = null
				}
			}
		}
		.onFocusChanged { state ->
			currentStep = if(isFocused && !state.isFocused && currentStep == StepBeforeSaveFocus.BOUNDS_NULL) {
				StepBeforeSaveFocus.FOCUS_FALSE
			} else null
			
			isFocused = state.isFocused
		}
}