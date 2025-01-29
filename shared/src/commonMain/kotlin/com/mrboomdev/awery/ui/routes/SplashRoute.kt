package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.ui.screens.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
class SplashRoute: BaseRoute() {
	@Composable
	override fun Content() {
		SplashScreen(
			modifier = Modifier
				.fillMaxSize()
		)
	}
}