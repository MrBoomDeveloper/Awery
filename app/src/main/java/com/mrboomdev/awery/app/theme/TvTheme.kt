package com.mrboomdev.awery.app.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@Composable
fun TvTheme(content: @Composable () -> Unit) {
	MaterialTheme(colorScheme = darkColorScheme()) {
		content()
	}
}