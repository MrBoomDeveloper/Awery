package com.mrboomdev.awery.ui.mobile

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.app.ThemeManager

@Composable
fun AweryTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		colors = if(ThemeManager.isDarkModeEnabled()) darkColors() else lightColors()
	) {
		content()
	}
}