package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.ui.screens.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Composable
fun SplashRoute.Content() {
	SplashScreen(
		modifier = Modifier
			.fillMaxSize()
	)
}