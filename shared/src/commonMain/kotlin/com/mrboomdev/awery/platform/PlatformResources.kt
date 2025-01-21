@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.mrboomdev.awery.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.replaceWithArgs
import org.jetbrains.compose.resources.stringResource

expect object PlatformResources {
	@OptIn(ExperimentalResourceApi::class)
	internal var resourceEnvironment: ResourceEnvironment?
}

private val stringValueCache = mutableMapOf<String, String>()
private val stringResClass = Class.forName("com.mrboomdev.awery.generated.CommonMainString0")

internal fun clearCache() {
	stringValueCache.clear()
}

@Suppress("UNCHECKED_CAST")
fun i18n(key: String): String? {
	stringValueCache[key]?.also {
		return it
	}
	
	// Do reflection magic
	return try {
		val delegate = stringResClass.getDeclaredField("$key\$delegate").apply {
			isAccessible = true
		}[null] as Lazy<StringResource>
		
		return i18n(delegate.value)
	} catch(_: NoSuchFieldException) { null }
}

fun i18n(key: String, vararg args: Any?): String? {
	return i18n(key)?.replaceWithArgs(args.map { it.toString() })
}

@OptIn(ExperimentalResourceApi::class)
fun i18n(res: StringResource): String {
	stringValueCache[res.key]?.also {
		return it
	}
	
	return runBlocking {
		PlatformResources.resourceEnvironment?.let { env ->
			getString(env, res)
		} ?: getString(res)
	}.also { result ->
		// Cache result, so that we don't have to load it again later.
		stringValueCache[res.key] = result
	}
}

fun i18n(res: StringResource, vararg args: Any?): String {
	return i18n(res).replaceWithArgs(args.map { it.toString() })
}