package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key.Companion.T
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mrboomdev.awery.utils.decodeUri
import com.mrboomdev.awery.utils.encodeUri
import com.mrboomdev.awery.utils.serializerByReflection
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.typeOf

private val cachedObjectInstances = mutableMapOf<KClass<*>, Any>()

@OptIn(ExperimentalEncodingApi::class)
@PublishedApi
internal fun <T> NavBackStackEntry.readData(
	isJavaSerializable: Boolean, 
	kotlinSerializer: KSerializer<T>?
): T {
	val data = arguments?.getString("data")?.decodeUri()
	
	val instance = if(data == null) {
		// No data provided. It means that there are simply no arguments.
		// Probably it is an object, so we can cache it for later.
		
		@Suppress("UNCHECKED_CAST")
		cachedObjectInstances.getOrPut(T::class) {
			@Suppress("DEPRECATION")
			T::class.java.newInstance()
		} as T
	} else {
		// We have to deal with serialization here 😅
		
		if(isJavaSerializable) {
			@Suppress("UNCHECKED_CAST")
			Base64.decode(data).let { bytes ->
				ObjectInputStream(ByteArrayInputStream(bytes)).use {
					it.readObject() as Serializable
				}
			} as T
		} else {
			Json.decodeFromString(kotlinSerializer!!, data)
		}
	}
	
	return instance
}

/**
 * Better solution than [composable] with automatic arguments serialization.
 * Useful for complex serialization.
 */
inline fun <reified T: Any> NavGraphBuilder.composable2(
	noinline content: @Composable (T) -> Unit
) {
	if(!T::class.isData) {
		throw IllegalArgumentException("T has to be an data class!")
	}
	
	if(T::class.isAbstract) {
		throw IllegalArgumentException("T cannot be instantiated because it is abstract!")
	}
	
	val supers = T::class.allSuperclasses
	val isJavaSerializable = supers.contains(Serializable::class)
	
	@Suppress("UNCHECKED_CAST") 
	val kotlinSerializer = serializerOrNull(typeOf<T>()) as KSerializer<T>?
	
	if(!isJavaSerializable && kotlinSerializer == null) {
		throw IllegalArgumentException("T isn't serializable!")
	}
	
	composable(
		route = buildString { 
			append(T::class.qualifiedName)
			
			if(!T::class.declaredMembers.isEmpty()) {
				append("/{data}")
			}
		},
		
		arguments = if(!T::class.declaredMembers.isEmpty()) {
			listOf(navArgument("data") {
				type = NavType.StringType
			})
		} else emptyList()
	) { entry ->
		content(entry.readData(isJavaSerializable, kotlinSerializer))
	}
}

@OptIn(ExperimentalEncodingApi::class)
fun NavController.navigate2(destination: Any) {
	val clazz = destination::class
	val supers = T::class.allSuperclasses
	
	val isJavaSerializable = supers.contains(Serializable::class)
	
	@Suppress("UNCHECKED_CAST") 
	val kotlinSerializer = clazz.serializerByReflection() as KSerializer<Any>?
	
	if(!isJavaSerializable && kotlinSerializer == null) {
		throw IllegalArgumentException("Destination isn't serializable!")
	}
	
	val route = buildString {
		append(clazz.qualifiedName)
		
		if(!clazz.declaredMembers.isEmpty()) {
			append("/")
			
			append(if(isJavaSerializable) {
				ByteArrayOutputStream().apply {
					ObjectOutputStream(this).use {
						it.writeObject(destination)
					}
				}.let { Base64.encode(it.toByteArray()) }
			} else {
				Json.encodeToString(kotlinSerializer!!, destination)
			}.encodeUri())
		}
	}
	
	navigate(route)
}