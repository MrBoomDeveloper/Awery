package com.mrboomdev.awery.extension.loaders.awery

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.resolve
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object AweryExtensionConstants {
	const val manifestPath = "manifest.json"
	const val iconPath = "icon.png"
	
	@OptIn(ExperimentalSerializationApi::class)
	val manifestJsonFormat = Json { 
		ignoreUnknownKeys = true
		allowTrailingComma = true
		allowComments = true
	}

	val installedDirectory by lazy {
		FileKit.filesDir / "extensions/installed/awery"
	}

	val settingsDirectory by lazy {
		FileKit.filesDir / "extensions/settings"
	}
}

expect val AweryExtensionConstants.platformBinary: String