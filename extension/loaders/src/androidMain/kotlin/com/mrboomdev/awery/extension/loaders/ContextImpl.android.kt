package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.android.AndroidUtils
import com.mrboomdev.awery.extension.sdk.Context
import com.mrboomdev.awery.extension.sdk.Preferences

actual class ContextImpl actual constructor(id: String): Context {
	override val preferences: Preferences by lazy {
		AndroidPreferences(
			AndroidUtils.getSharedPreferences("awery_source_$id")
		)
	}
}