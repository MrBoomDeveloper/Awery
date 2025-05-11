package com.mrboomdev.awery.ui.routes

import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogVideoFile
import kotlinx.serialization.Serializable

@Serializable
class PlayerRoute(
	val media: CatalogMedia,
	val initialEpisode: Int,
	val episodes: List<CatalogVideoFile>,
)