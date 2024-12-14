package com.mrboomdev.awery.platform

expect object PlatformResources {
	fun i18n(key: String, vararg args: Any): String?
}

internal fun getResourceId(type: Class<*>, res: String) = try {
	type.getDeclaredField(res)[null] as? Int
} catch(e: NoSuchFieldException) { null }