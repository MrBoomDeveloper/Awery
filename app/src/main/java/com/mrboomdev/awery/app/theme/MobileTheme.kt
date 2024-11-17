package com.mrboomdev.awery.app.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun MobileTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		colors = if(ThemeManager.isDarkModeEnabled()) darkColors() else lightColors()
	) {
		content()
	}
}