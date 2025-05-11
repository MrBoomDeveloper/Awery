package com.mrboomdev.awery.ui

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
import com.google.android.material.color.DynamicColors
import com.mrboomdev.awery.app.theme.LocalAweryTheme
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.Platform.TV

@Composable
fun AweryRootAndroid(content: @Composable () -> Unit) {
	MobileTheme(
		palette = AwerySettings.THEME_COLOR_PALETTE.value.let {
			if(DynamicColors.isDynamicColorAvailable() && !TV) {
				it ?: AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU
			} else it?.let {
				// Material You isn't supported on this device,
				// so we do fallback to the default value.
				if(it == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU) null else it
			} ?: AwerySettings.ThemeColorPaletteValue.RED
		},
		
		isDark = LocalAweryTheme.current.isDark,
		isAmoled = LocalAweryTheme.current.isAmoled,
		content = content
	)
}

@Composable
private fun getColorSchemeValue(
	palette: AwerySettings.ThemeColorPaletteValue,
	isDark: Boolean
): ColorScheme {
	if(palette == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU) {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
			throw UnsupportedOperationException("Material Your isn't available on your old device!")
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
	}.let { if(isDark) {
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
private fun MobileTheme(
	palette: AwerySettings.ThemeColorPaletteValue,
	isDark: Boolean,
	isAmoled: Boolean,
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colorScheme = getColorSchemeValue(palette, isDark).let {
			if(isAmoled) it.copy(
				surface = Color.Black,
				background = Color.Black
			) else it
		}
	) {
		content()
	}
}