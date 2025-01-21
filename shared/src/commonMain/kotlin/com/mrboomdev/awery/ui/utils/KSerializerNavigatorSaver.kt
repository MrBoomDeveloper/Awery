package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.listSaver
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.NavigatorSaver
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer

@OptIn(ExperimentalVoyagerApi::class)
class KSerializerNavigatorSaver: NavigatorSaver<Any> {
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
					put("class", it::class.qualifiedName!!)
					put("it", Json.encodeToString(serializer(it::class.java), it))
				}.toString()
			}
		},
		
		restore = { items ->
			val decoded = items.map { 
				Json.parseToJsonElement(it).let { json ->
					Json.decodeFromString(
						serializer(Class.forName(json.jsonObject["class"]!!.jsonPrimitive.contentOrNull!!)),
						json.jsonObject["it"]!!.jsonPrimitive.contentOrNull!!
					)
				}
			}
			
			@Suppress("UNCHECKED_CAST")
			Navigator(decoded as List<Screen>, key, stateHolder, disposeBehavior, parent)
		}
	)
}