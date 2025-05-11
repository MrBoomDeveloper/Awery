package com.mrboomdev.awery.app.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.ui.Themes

@Composable
private fun getOriginalColorScheme(
	palette: AwerySettings.ThemeColorPaletteValue,
	isDark: Boolean
): ColorScheme {
	if(palette == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU) {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
			throw UnsupportedOperationException("How did you even done that?")
		}
			
		return with(LocalContext.current) {
			if(isDark) dynamicDarkColorScheme(this)
			else dynamicLightColorScheme(this)
		}
	}
	
	return when(palette) {
		AwerySettings.ThemeColorPaletteValue.GREEN -> Themes.Green
		AwerySettings.ThemeColorPaletteValue.BLUE -> Themes.Blue
		else -> Themes.Red
	}.let { if(isDark || Platform.TV) {
		darkColorScheme(
			surface = it.dark.background,
			background = it.dark.background
		)
	} else {
		lightColorScheme(
			surface = it.light.background,
			background = it.light.background
		)
	}}
}

@Composable
fun MobileTheme(
	palette: AwerySettings.ThemeColorPaletteValue,
	isDark: Boolean,
	isAmoled: Boolean,
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colorScheme = getOriginalColorScheme(palette, isDark).let {
			if(isAmoled) it.copy(
				surface = Color.Black,
				background = Color.Black
			) else it
		}
	) {
		content()
	}
}