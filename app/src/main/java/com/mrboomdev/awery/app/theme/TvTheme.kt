package com.mrboomdev.awery.app.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import androidx.tv.material3.darkColorScheme

@Composable
fun TvTheme(content: @Composable () -> Unit) {
	TvMaterialTheme(colorScheme = darkColorScheme()) {
		MaterialTheme(colors = darkColors()) {
			content()
		}
	}
}