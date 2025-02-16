package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogVideoFile
import com.mrboomdev.awery.ui.screens.player.PlayerScreen
import kotlinx.serialization.Serializable

@Serializable
class PlayerRoute(
	val media: CatalogMedia,
	val initialEpisode: Int,
	val episodes: List<CatalogVideoFile>,
): BaseRoute() {
	@Composable
	override fun Content() {
		PlayerScreen(
			media,
			initialEpisode = initialEpisode,
			episodes = episodes
		)
	}
}