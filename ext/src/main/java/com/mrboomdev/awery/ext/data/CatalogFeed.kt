package com.mrboomdev.awery.ext.data

import com.squareup.moshi.Json
import java.io.Serializable

class CatalogFeed(
	@Json(name = "source_manager")
	val managerId: String,
	@Json(name = "source_id")
	val sourceId: String,
	@Json(name = "source_feed")
	val feedId: String?,
	val title: String,
	val style: Style = Style.UNSPECIFIED
): Serializable {
	class Loaded(
		val feed: CatalogFeed,
		val items: CatalogSearchResults<CatalogMedia>? = null,
		val throwable: Throwable? = null
	): Serializable

	enum class Style {
		UNSPECIFIED, SLIDER, ROW, GRID
	}
}