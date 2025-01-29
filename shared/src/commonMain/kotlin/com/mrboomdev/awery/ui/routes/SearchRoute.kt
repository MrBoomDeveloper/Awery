package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.screens.SearchScreen
import kotlinx.serialization.Serializable

@Serializable
class SearchRoute: BaseRoute() {
	@Composable
	override fun Content() {
		SearchScreen()
	}
}