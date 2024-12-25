package com.mrboomdev.awery.gradle.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

val JsonElement?.isNull: Boolean
	get() = this == null || this is JsonNull

val JsonElement.textContent: String?
	get() = if(this is JsonPrimitive) contentOrNull else null

val JsonElement.items: JsonArray?
	get() = if(this is JsonArray) this else null