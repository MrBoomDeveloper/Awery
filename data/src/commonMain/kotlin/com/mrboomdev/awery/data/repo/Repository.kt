package com.mrboomdev.awery.data.repo

import com.mrboomdev.awery.data.database.entity.DBRepository

data class Repository(
	val info: DBRepository,
	val items: List<Item>
) {
	data class Item(
		val id: String,
		val name: String,
		val version: String,
		val lang: String,
		val url: String,
		val icon: String?,
		val isNsfw: Boolean
	)
}