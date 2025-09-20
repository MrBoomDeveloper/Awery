package com.mrboomdev.awery.extension.loaders.awery

import kotlinx.serialization.Serializable

@Serializable
class AweryExtensionManifest(
	val main: String,
	val name: String,
	val id: String,
	val version: String = "1.0.0",
	val nsfw: Boolean,
	val lang: String? = null,
	val icon: String? = null,
	val webpage: String? = null
)