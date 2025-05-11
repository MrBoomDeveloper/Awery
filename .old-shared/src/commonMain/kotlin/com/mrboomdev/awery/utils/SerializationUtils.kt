package com.mrboomdev.awery.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.jvm.jvmErasure

@Suppress("UNCHECKED_CAST")
fun <T : Enum<T>> enumStateSaver() = Saver<MutableState<T>, String>(
	save = { "${it.value::class.qualifiedName}.${it.value.name}" },
	restore = {
		val className = it.substringBeforeLast(".")
		val value = it.substringAfterLast(".")
		val clazz = Class.forName(className) as Class<out Enum<*>>
		mutableStateOf(java.lang.Enum.valueOf(clazz, value) as T)
	}
)

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
		return Json.decodeFromString(serializer!!, json) as T
	}
}

/**
 * May be useful if we'll only know a real type at the runtime.
 */
inline fun <reified T : Any> reflectionSerializerOf(): KSerializer<T> = object : ReflectionSerializer<T>() {}

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
fun <T: Any> KClass<T>.serializerByReflection(): KSerializer<T>? {
	try {
		// Is this class directly marked as serializable?
		companionObjectInstance?.serializer?.also {
			return it as KSerializer<T>
		}
	} catch(_: NoSuchMethodException) {}
	
	for(superType in supertypes) {
		// Maybe one of it's parents is serializable?
		try {
			val serializer = superType.jvmErasure.serializerByReflection()
			if(serializer != null) return serializer as KSerializer<T>
		} catch(_: IllegalStateException) { }
	}
	
	return null
}

private val Any.serializer get() = this::class.java.let { clazz ->
	clazz.getMethod("serializer").apply {
		isAccessible = true
	}.invoke(this) as KSerializer<*>
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