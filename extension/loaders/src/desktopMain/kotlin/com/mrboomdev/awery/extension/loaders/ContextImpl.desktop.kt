package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.core.utils.toJavaFile
import com.mrboomdev.awery.extension.loaders.awery.AweryExtensionConstants
import com.mrboomdev.awery.extension.sdk.Context
import com.mrboomdev.awery.extension.sdk.Preferences
import com.russhwolf.settings.PropertiesSettings
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.resolve
import java.io.FileReader
import java.util.Properties

actual class ContextImpl actual constructor(id: String): Context {
	override val preferences: Preferences by lazy {
		DesktopPreferences(
			PropertiesSettings(
				delegate = Properties().apply {
					val file = AweryExtensionConstants.settingsDirectory.resolve(id).apply { 
						parent()?.createDirectories()
					}.toJavaFile().apply { 
						if(!exists()) {
							createNewFile()
						}
					}
					
					load(FileReader(file))
				}
			)
		)
	}
}