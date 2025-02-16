package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ui.screens.SearchScreen
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class SearchRoute(
	private val initialFilters: List<@Contextual Setting>? = null
): BaseRoute() {
	@Composable
	override fun Content() {
		val navigation = LocalNavigator.currentOrThrow
		
		SearchScreen(
			onBack = { navigation.pop() },
			initialFilters = initialFilters
		)
	}
}