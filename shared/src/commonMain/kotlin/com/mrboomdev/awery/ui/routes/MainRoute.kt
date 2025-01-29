package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.ui.screens.MainScreen
import kotlinx.serialization.Serializable

@Serializable
expect class MainRoute(): DefaultMainRoute

@Serializable
open class DefaultMainRoute: BaseRoute() {
	@Composable
	override fun Content() {
		MainScreen(
			modifier = Modifier.fillMaxSize()
		)
	}
}