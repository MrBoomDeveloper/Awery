package com.mrboomdev.awery.extension.loaders.awery

import com.mrboomdev.awery.extension.sdk.Extension
import dalvik.system.PathClassLoader
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath

internal actual fun loadMain(
	parent: Extension,
	binary: PlatformFile,
	main: String
): Extension {
	return PathClassLoader(
		binary.absolutePath(),
		ResolvedExtensionParent::class.java.classLoader
	).loadClass(main)!!
		.getConstructor(Extension::class.java)
		.newInstance(parent) as Extension
}