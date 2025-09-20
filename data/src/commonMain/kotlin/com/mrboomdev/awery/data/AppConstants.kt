package com.mrboomdev.awery.data

import com.mrboomdev.awery.core.Awery

val Awery.appVersion: String get() = AweryBuildConfig.appVersion
val Awery.appVersionCode: Int get() = AweryBuildConfig.appVersionCode
val Awery.sdkVersion: String get() = AweryBuildConfig.extVersion
expect val Awery.isDebug: Boolean