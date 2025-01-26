package com.mrboomdev.awery.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

@Serializable
private data class JsonValueWrapper(
	val clazz: String,
	val json: String
) {
	@Suppress("UNCHECKED_CAST")
	constructor(it: Any): this(
		it::class.qualifiedName!!, 
		Json.encodeToString(it::class.serializerByReflection() as KSerializer<Any>, it)
	)
	
	@Suppress("UNCHECKED_CAST")
	fun <T> deserialize(): T {
		val serializer = Class.forName(clazz).kotlin.serializerByReflection()
		return Json.decodeFromString(serializer, json) as T
	}
}

abstract class ReflectionSerializer<T: Any>: KSerializer<T> {
	override val descriptor: SerialDescriptor
		get() = JsonValueWrapper.serializer().descriptor
			
	override fun deserialize(decoder: Decoder): T {
		return decoder.decodeSerializableValue(JsonValueWrapper.serializer()).deserialize()
	}
	
	override fun serialize(encoder: Encoder, value: T) {
		val wrapper = JsonValueWrapper(value)
		encoder.encodeSerializableValue(JsonValueWrapper.serializer(), wrapper)
	}
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> KClass<T>.serializerByReflection(): KSerializer<T> {
	val companionClass = Class.forName("$qualifiedName\$Companion", true, java.classLoader)
	val companionInstance = java.getField("Companion")[null]
	
	return companionClass.getMethod("serializer").apply {
		isAccessible = true
	}.invoke(companionInstance) as KSerializer<T>
}

@RequiresOptIn(
	message = "This value and all it's members have to be serializable! Opt in only if it so.",
	level = RequiresOptIn.Level.ERROR)
@Target(
	AnnotationTarget.CLASS,
	AnnotationTarget.PROPERTY,
	AnnotationTarget.FIELD,
	AnnotationTarget.VALUE_PARAMETER,
	AnnotationTarget.CONSTRUCTOR,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_SETTER)
annotation class SerializableRequired