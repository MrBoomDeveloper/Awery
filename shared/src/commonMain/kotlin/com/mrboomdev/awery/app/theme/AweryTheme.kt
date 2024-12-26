package com.mrboomdev.awery.app.theme

import androidx.compose.runtime.compositionLocalOf

interface AweryTheme {
	var isDark: Boolean
	var isAmoled: Boolean
}

val LocalAweryTheme = compositionLocalOf<AweryTheme> {
	error("No AweryTheme was defined in the scope!")
}