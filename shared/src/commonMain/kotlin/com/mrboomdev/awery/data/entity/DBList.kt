package com.mrboomdev.awery.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DBList(
	@PrimaryKey(true) val id: Long,
	val name: String,
	val icon: String? = null
) {
	companion object {
		const val LIST_HIDDEN = -1L
		const val LIST_HISTORY = -2L
	}
}