package com.mrboomdev.awery.sources.jvm

import kotlinx.serialization.Serializable

@Serializable
class JvmEntry(
	val id: String,
	val main: String,
	val name: String?,
	val icon: String?,
	val type: Type,
	val ageRating: String?,
	val features: Array<String>
) {
	enum class Type {
		SOURCE, MANAGER
	}
}