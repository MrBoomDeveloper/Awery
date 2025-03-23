package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.ui.utils.WINDOW_SIZE_MEDIUM
import com.mrboomdev.awery.ui.utils.WindowSizeClassValue
import com.mrboomdev.awery.ui.utils.compareTo

/**
 * @param horizontalWindowSizeBreakpoint If screen of this size or larger, horizontal layout would be selected.
 */
@Composable
fun Fitter(
	modifier: Modifier = Modifier,
	spacing: Dp = 16.dp,
	@WindowSizeClassValue horizontalWindowSizeBreakpoint: Int = WINDOW_SIZE_MEDIUM,
	primaryContent: @Composable () -> Unit,
	secondaryContent: @Composable () -> Unit
) {
	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	
	if(windowSizeClass >= horizontalWindowSizeBreakpoint) {
		Row(modifier = modifier) { 
			primaryContent()
			Spacer(Modifier.width(spacing))
			secondaryContent()
		}
	} else {
		Column(modifier = modifier) {
			primaryContent()
			Spacer(Modifier.height(spacing))
			secondaryContent()
		}
	}
}