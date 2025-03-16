package com.mrboomdev.awery.ui.utils

import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import com.mrboomdev.awery.utils.decodeUri
import com.mrboomdev.awery.utils.encodeUri
import com.mrboomdev.awery.utils.serializerByReflection
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.typeOf
import java.io.Serializable as JavaSerializable

class NavTypesBuilderScope(@PublishedApi internal val map: MutableMap<KType, NavType<*>>) {
	inline fun <reified T : Any> navType() {
		map[typeOf<T>()] = navTypeOf<T>()
	}
	
	inline fun <reified T: Any> navType(vararg arguments: KSerializer<*>) {
		map[typeOf<T>()] = navTypeOf<T>(*arguments)
	}
}

fun buildNavTypes(scope: NavTypesBuilderScope.() -> Unit) = buildMap { 
	scope(NavTypesBuilderScope(this))
}

inline fun <reified T: Any> navTypeOf() = 
	navTypeOfImpl(T::class, typeOf<T>(), null)

inline fun <reified T: Any> navTypeOf(vararg arguments: KSerializer<*>) = 
	navTypeOfImpl(T::class, typeOf<T>(), arguments.toList())

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal fun <T: Any> navTypeOfImpl(
	clazz: KClass<T>,
	type: KType?,
	args: List<KSerializer<*>>?
): NavType<T> {
	val supers = clazz.allSuperclasses
	
	if(supers.contains(JavaSerializable::class)) {
		@Suppress("UNCHECKED_CAST")
		return JavaSerializableNavType as NavType<T>
	}
	
	clazz.serializerByReflection()?.also { 
		return KotlinSerializableNavType(it)
	}
	
	throw IllegalArgumentException("${clazz.qualifiedName} isn't serializable!")
}

@PublishedApi
internal class KotlinSerializableNavType<T>(
	private val serializer: KSerializer<T>
): NavType<T>(true) {
	override fun get(bundle: Bundle, key: String): T? =
		bundle.getString(key)?.let { parseValue(it) }
	
	override fun put(bundle: Bundle, key: String, value: T) =
		bundle.putString(key, serializeAsValue(value))
	
	override fun parseValue(value: String): T = 
		Json.decodeFromString(serializer, value.decodeUri())
	
	override fun serializeAsValue(value: T): String = 
		Json.encodeToString(serializer, value).encodeUri()
}

internal object JavaSerializableNavType: NavType<JavaSerializable>(true) {
	override fun get(bundle: Bundle, key: String): JavaSerializable? = 
		bundle.getString(key)?.let { parseValue(it) }
	
	override fun put(bundle: Bundle, key: String, value: JavaSerializable) =
		bundle.putString(key, serializeAsValue(value))
	
	@OptIn(ExperimentalEncodingApi::class)
	override fun parseValue(value: String): JavaSerializable = Base64.decode(value.decodeUri()).let { bytes ->
		ObjectInputStream(ByteArrayInputStream(bytes)).use {
			it.readObject() as JavaSerializable
		}
	}
	
	@OptIn(ExperimentalEncodingApi::class)
	override fun serializeAsValue(value: JavaSerializable): String = ByteArrayOutputStream().apply {
		ObjectOutputStream(this).use {
			it.writeObject(value)
		}
	}.let { Base64.encode(it.toByteArray()) }.encodeUri()
}