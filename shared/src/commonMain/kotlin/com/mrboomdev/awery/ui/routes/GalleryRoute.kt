package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
class GalleryRoute(
	val images: List<String>,
	val initialImage: Int = 0
): BaseRoute() {
	@Composable
	override fun Content() {
		TODO("Not yet implemented")
	}
}