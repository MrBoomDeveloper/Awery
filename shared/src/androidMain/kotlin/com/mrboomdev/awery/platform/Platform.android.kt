package com.mrboomdev.awery.platform

import android.app.UiModeManager
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.core.content.getSystemService
import com.google.android.material.color.DynamicColors
import com.mrboomdev.awery.platform.android.AndroidGlobals
import com.mrboomdev.awery.shared.BuildConfig

actual object Platform {
	actual val NAME = "Android"

	@Suppress("DEPRECATION")
	val TV by lazy { AndroidGlobals.applicationContext.let {
		it.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
				|| it.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
				|| it.getSystemService<UiModeManager>()!!.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
				|| !it.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
	}}

	actual fun isRequirementMet(requirement: String): Boolean {
		var mRequirement = requirement
		var invert = false

		if(mRequirement.startsWith("!")) {
			invert = true
			mRequirement = mRequirement.substring(1)
		}

		val result = when(mRequirement) {
			"material_you" -> DynamicColors.isDynamicColorAvailable()
			"tv" -> TV
			"beta" -> AndroidGlobals.applicationContext.packageName != "com.mrboomdev.awery"
			"debug" -> BuildConfig.DEBUG
			else -> false
		}

		return if(invert) !result else result
	}
}