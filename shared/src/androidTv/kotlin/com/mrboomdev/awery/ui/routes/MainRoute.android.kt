package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.screens.TvMainScreen
import kotlinx.serialization.Serializable

@Serializable
actual class MainRoute : DefaultMainRoute() {
	@Composable
	override fun Content() {
		TvMainScreen()
	}
}