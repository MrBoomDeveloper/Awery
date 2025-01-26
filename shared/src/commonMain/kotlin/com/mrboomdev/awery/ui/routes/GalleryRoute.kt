package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.serialization.Serializable

@Serializable
class GalleryRoute(
	val images: List<String>,
	val initialImage: Int = 0
): Screen {
	@Composable
	override fun Content() {
		TODO("Not yet implemented")
	}
}