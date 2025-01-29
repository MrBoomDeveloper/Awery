package com.mrboomdev.awery.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DBRepository(
	@PrimaryKey val url: String,
	val isEnabled: Boolean
)