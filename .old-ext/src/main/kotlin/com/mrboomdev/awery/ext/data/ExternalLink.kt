package com.mrboomdev.awery.ext.data

import kotlinx.serialization.Serializable

@Serializable
data class ExternalLink(
	val title: String,
	val link: String,
	val icon: String
): java.io.Serializable