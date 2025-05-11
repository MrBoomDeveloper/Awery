package com.mrboomdev.awery.platform

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ThemeQualifier

actual object PlatformResources {
	@OptIn(ExperimentalResourceApi::class)
	internal actual var resourceEnvironment: ResourceEnvironment? = null

	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	@OptIn(InternalResourceApi::class, ExperimentalResourceApi::class)
	fun load(context: Context) {
		clearCache()
		
		val composeLocale = Locale.current
		val composeDensity = Density(context)

		val composeTheme = with(context.resources.configuration.uiMode) {
			(this and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
		}

		resourceEnvironment = ResourceEnvironment(
			LanguageQualifier(composeLocale.language),
			RegionQualifier(composeLocale.region),
			ThemeQualifier.selectByValue(composeTheme),
			DensityQualifier.selectByDensity(composeDensity.density)
		)
	}
}