package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ui.screens.MediaScreen
import com.mrboomdev.awery.ui.screens.MediaScreenTab
import kotlinx.serialization.Serializable

@Serializable
data class MediaRoute(
	val media: CatalogMedia,
	val initialTab: MediaScreenTab = MediaScreenTab.INFO
)

@Composable
fun MediaRoute.Companion.Content(args: MediaRoute) {
	MediaScreen(
		args.media, args.initialTab
	)
}