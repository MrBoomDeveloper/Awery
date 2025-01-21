package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.mrboomdev.awery.ui.screens.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
class SplashRoute: Screen {
	@Composable
	override fun Content() {
		SplashScreen(
			modifier = Modifier
				.fillMaxSize()
		)
	}
}