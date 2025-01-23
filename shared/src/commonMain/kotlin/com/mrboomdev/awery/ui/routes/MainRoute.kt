package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.mrboomdev.awery.ui.screens.MainScreen
import kotlinx.serialization.Serializable

@Serializable
class MainRoute: Screen {
	@Composable
	override fun Content() {
		MainScreen(
			modifier = Modifier.fillMaxSize()
		)
	}
}