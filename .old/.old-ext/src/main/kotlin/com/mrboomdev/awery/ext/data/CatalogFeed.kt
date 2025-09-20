package com.mrboomdev.awery.ext.data

import kotlinx.serialization.Contextual
import java.io.Serializable

@kotlinx.serialization.Serializable
class CatalogFeed(
	val managerId: String? = null,
	val sourceId: String? = null,
	val feedId: String? = null,
	val title: String,
	val style: Style = Style.ROW,
	val hideIfEmpty: Boolean = false,
	val filters: List<@Contextual Setting>? = null
): Serializable {
	sealed interface Style {
		data object SLIDER: Style
		data object ROW: Style
		data object COLUMN: Style
	}
}