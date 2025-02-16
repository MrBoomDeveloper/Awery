package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ui.screens.MediaScreen
import com.mrboomdev.awery.ui.screens.MediaScreenTab
import kotlinx.serialization.Serializable

@Serializable
class MediaRoute(
	val media: CatalogMedia,
	val initialTab: MediaScreenTab
): BaseRoute() {
	@Composable
	override fun Content() {
		MediaScreen(
			media, initialTab
		)
	}
}