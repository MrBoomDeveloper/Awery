package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.listSaver
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.NavigatorSaver
import com.mrboomdev.awery.utils.serializerByReflection
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@OptIn(ExperimentalVoyagerApi::class)
class KSerializerNavigatorSaver(
	val serializer: (Any) -> KSerializer<out Any> = {
		it::class.serializerByReflection()
	}
): NavigatorSaver<Any> {
	@OptIn(InternalVoyagerApi::class)
	override fun saver(
		initialScreens: List<Screen>, 
		key: String, 
		stateHolder: SaveableStateHolder, 
		disposeBehavior: NavigatorDisposeBehavior, 
		parent: Navigator?
	) = listSaver(
		save = { navigator ->
			navigator.items.map { 
				buildJsonObject {
					@Suppress("UNCHECKED_CAST")
					put("it", Json.encodeToString(serializer(it) as KSerializer<Any>, it))
					put("class", it::class.qualifiedName!!)
				}.toString()
			}
		},
		
		restore = { items ->
			val decoded = items.map { json ->
				Json.parseToJsonElement(json).let { jsonElement ->
					val deserializer = jsonElement.jsonObject["class"]!!.jsonPrimitive.contentOrNull!!.let {
						Class.forName(it).kotlin.serializerByReflection()
					}
					
					Json.decodeFromString(
						deserializer,
						jsonElement.jsonObject["it"]!!.jsonPrimitive.contentOrNull!!
					)
				}
			}
			
			@Suppress("UNCHECKED_CAST")
			Navigator(decoded as List<Screen>, key, stateHolder, disposeBehavior, parent)
		}
	)
}