package com.mrboomdev.awery.platform

import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.mrboomdev.awery.app.AndroidGlobals

actual object PlatformResources {
	private val stringClass = Class.forName("com.mrboomdev.awery.R\$string")

	actual fun i18n(key: String, vararg args: Any): String? {
		return i18n(getResourceId(stringClass, key) ?: return null, *args)
	}

	fun i18n(@StringRes res: Int, vararg args: Any): String {
		return ContextCompat.getContextForLanguage(AndroidGlobals.applicationContext).getString(res, *args)
	}
}