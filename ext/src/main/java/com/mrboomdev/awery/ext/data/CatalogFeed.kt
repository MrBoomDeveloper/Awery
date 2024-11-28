package com.mrboomdev.awery.ext.data

import com.squareup.moshi.Json
import java.io.Serializable

class CatalogFeed(
	@Json(name = "source_manager")
	val managerId: String? = null,
	@Json(name = "source_id")
	val sourceId: String? = null,
	@Json(name = "source_feed")
	val feedId: String? = null,
	val title: String,
	val style: Style = Style.UNSPECIFIED,
	@Json(name = "hide_if_empty")
	val hideIfEmpty: Boolean = false,
	val filters: Settings? = null
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