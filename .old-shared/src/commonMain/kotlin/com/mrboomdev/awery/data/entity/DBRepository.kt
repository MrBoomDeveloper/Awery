package com.mrboomdev.awery.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DBRepository(
	@PrimaryKey val url: String,
	val manager: String,
	val isEnabled: Boolean = true,
	val title: String? = null,
	val description: String? = null,
	val author: String? = null
)