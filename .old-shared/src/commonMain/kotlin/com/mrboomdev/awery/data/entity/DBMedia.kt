package com.mrboomdev.awery.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.util.GlobalId

@Entity
data class DBMedia(
	@PrimaryKey val globalId: String,
	val title: String,
	val ids: Map<String, String>,
	val extras: Map<String, String>
) {
	fun asCatalogMedia() = CatalogMedia(
		globalId = GlobalId(globalId),
		title = title,
		ids = ids,
		extras = extras
	)
}

internal fun CatalogMedia.asDBMedia() = DBMedia(
	globalId = globalId.value,
	title = title,
	ids = ids,
	extras = extras
)