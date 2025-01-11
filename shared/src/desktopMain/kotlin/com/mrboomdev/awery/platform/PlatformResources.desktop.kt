package com.mrboomdev.awery.platform

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceEnvironment

actual object PlatformResources {
	@OptIn(ExperimentalResourceApi::class)
	internal actual var resourceEnvironment: ResourceEnvironment? = null

	fun load() {
		throw NotImplementedError()
	}
}