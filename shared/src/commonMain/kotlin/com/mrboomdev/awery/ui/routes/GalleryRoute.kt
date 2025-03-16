package com.mrboomdev.awery.ui.routes

import kotlinx.serialization.Serializable

@Serializable
class GalleryRoute(
	val images: List<String>,
	val initialImage: Int = 0
)

