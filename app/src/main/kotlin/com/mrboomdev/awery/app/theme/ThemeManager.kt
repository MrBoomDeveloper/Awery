package com.mrboomdev.awery.app.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.isTv
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.AwerySettings
import com.mrboomdev.awery.AwerySettings.ThemeColorPaletteValue

object ThemeManager {
	val currentColorPalette: ThemeColorPaletteValue
		get() = AwerySettings.THEME_COLOR_PALETTE.value ?: resetPalette()

	val isDarkModeEnabled: Boolean
		get() {
			val config = anyContext.resources.configuration
			return (config.uiMode and Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES
		}

	fun Application.applyTheme() {
		var isDarkModeEnabled = AwerySettings.USE_DARK_THEME.value

		// Light theme on tv is really a bad thing.
		if(isTv) isDarkModeEnabled = true

		if(isDarkModeEnabled != null) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				getSystemService(UiModeManager::class.java)
					.setApplicationNightMode(
						if(isDarkModeEnabled) UiModeManager.MODE_NIGHT_YES
						else UiModeManager.MODE_NIGHT_NO
					)
			} else {
				AppCompatDelegate.setDefaultNightMode(
					if(isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
					else AppCompatDelegate.MODE_NIGHT_NO
				)
			}
		}
	}

	fun Activity.applyTheme() {
		val isAmoled = AwerySettings.USE_AMOLED_THEME.value == true
		val palette = currentColorPalette

		if(palette == ThemeColorPaletteValue.MATERIAL_YOU) {
			return DynamicColors.applyToActivityIfAvailable(this,
				DynamicColorsOptions.Builder().apply {
					if(isAmoled) {
						setThemeOverlay(R.style.AmoledThemeOverlay)
					}
				}.build())
		}

		setTheme(getThemeRes(palette, isAmoled && isDarkModeEnabled))
	}

	/**
	 * Set an composable component as activity's content and apply an current theme.
	 */
	fun ComponentActivity.setThemedContent(content: @Composable () -> Unit) {
		val isReallyAmoled = isDarkModeEnabled && AwerySettings.USE_AMOLED_THEME.value == true

		setContent {
			MobileTheme(
				palette = currentColorPalette,
				isDark = isDarkModeEnabled,
				isAmoled = isReallyAmoled
			) {
				TvTheme(
					palette = currentColorPalette,
					isDark = isDarkModeEnabled,
					isAmoled = isReallyAmoled
				) {
					content()
				}
			}
		}
	}

	@SuppressLint("PrivateResource")
	fun getThemeRes(theme: ThemeColorPaletteValue, isAmoled: Boolean): Int {
		// Amoled theme breaks some colors in a light theme.
		val isReallyAmoled = isAmoled && isDarkModeEnabled

		return when(theme) {
			ThemeColorPaletteValue.RED -> if(isReallyAmoled) R.style.Theme_Awery_Red_Amoled else R.style.Theme_Awery_Red
			ThemeColorPaletteValue.PINK -> if(isReallyAmoled) R.style.Theme_Awery_Pink_Amoled else R.style.Theme_Awery_Pink
			ThemeColorPaletteValue.PURPLE -> if(isReallyAmoled) R.style.Theme_Awery_Purple_Amoled else R.style.Theme_Awery_Purple
			ThemeColorPaletteValue.BLUE -> if(isReallyAmoled) R.style.Theme_Awery_Blue_Amoled else R.style.Theme_Awery_Blue
			ThemeColorPaletteValue.GREEN -> if(isReallyAmoled) R.style.Theme_Awery_Green_Amoled else R.style.Theme_Awery_Green
			ThemeColorPaletteValue.MONOCHROME -> if(isReallyAmoled) R.style.Theme_Awery_Monochrome_Amoled else R.style.Theme_Awery_Monochrome
			ThemeColorPaletteValue.MATERIAL_YOU -> com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight
		}
	}

	private fun resetPalette(): ThemeColorPaletteValue {
		val isMaterialYouSupported = DynamicColors.isDynamicColorAvailable()

		val theme = if(isMaterialYouSupported) {
			ThemeColorPaletteValue.MATERIAL_YOU
		} else {
			ThemeColorPaletteValue.RED
		}

		AwerySettings.THEME_COLOR_PALETTE.value = theme
		return theme
	}
}