@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.mrboomdev.awery.platform

import com.mrboomdev.awery.platform.PlatformResources.resourceEnvironment
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.StringItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPlatformResourceReader
import org.jetbrains.compose.resources.getResourceItemByEnvironment
import org.jetbrains.compose.resources.getStringItem
import org.jetbrains.compose.resources.replaceWithArgs

private val stringResourcesClass = Class.forName("com.mrboomdev.awery.generated.CommonMainString0")

expect object PlatformResources {
	@OptIn(ExperimentalResourceApi::class)
	internal var resourceEnvironment: ResourceEnvironment?
}

@Suppress("UNCHECKED_CAST")
fun i18n(key: String, vararg args: Any?): String? {
	val lazy = try {
		stringResourcesClass.getDeclaredField("$key\$delegate")[null] as? Lazy<StringResource>
	} catch(e: NoSuchFieldException) { null } ?: return null

	return i18n(lazy.value, *args)
}

@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
fun i18n(res: StringResource, vararg args: Any?): String {
	val resourceReader = getPlatformResourceReader()
	val environment = requireNotNull(resourceEnvironment) { "PlatformResources.resourceEnvironment wasn't been loaded!" }

	val resourceItem = res.getResourceItemByEnvironment(environment)
	val item = runBlocking { getStringItem(resourceItem, resourceReader) as StringItem.Value }
	val text = item.text

	if(args.isNotEmpty()) {
		return text.replaceWithArgs(args.map { it.toString() })
	}

	return text
}