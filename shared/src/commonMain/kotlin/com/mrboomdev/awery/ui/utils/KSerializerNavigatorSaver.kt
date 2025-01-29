package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.listSaver
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.NavigatorSaver
import com.mrboomdev.awery.ui.routes.SettingsRoute
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
	val serializer: (Any) -> KSerializer<out Any> = shit@{
		if(it is SettingsRoute) {
			return@shit SettingsRoute.serializer()
		}
		
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
			val decoded = items.map { 
				Json.parseToJsonElement(it).let { json ->
					Json.decodeFromString(
						kotlinx.serialization.serializer(Class.forName(json.jsonObject["class"]!!.jsonPrimitive.contentOrNull!!)),
						json.jsonObject["it"]!!.jsonPrimitive.contentOrNull!!
					)
				}
			}
			
			@Suppress("UNCHECKED_CAST")
			Navigator(decoded as List<Screen>, key, stateHolder, disposeBehavior, parent)
		}
	)
}