package com.mrboomdev.awery.ext.data

import java.io.Serializable

class CatalogFeed(
	val managerId: String? = null,
	val sourceId: String? = null,
	val feedId: String? = null,
	val title: String,
	val style: Style = Style.UNSPECIFIED,
	val hideIfEmpty: Boolean = false,
	val filters: List<Setting>? = null
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