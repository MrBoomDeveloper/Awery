package com.mrboomdev.awery.data

import com.mrboomdev.awery.core.Awery

actual val Awery.isDebug: Boolean
	get() = AweryBuildConfig.debug || isAndroidDebug

private val isAndroidDebug by lazy { 
	Class.forName("com.mrboomdev.awery.BuildConfig").getField("DEBUG").get(null) == true
}