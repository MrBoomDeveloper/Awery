package com.mrboomdev.awery.data

import com.mrboomdev.awery.core.Awery

actual val Awery.isDebug: Boolean
	get() = AweryBuildConfig.debug