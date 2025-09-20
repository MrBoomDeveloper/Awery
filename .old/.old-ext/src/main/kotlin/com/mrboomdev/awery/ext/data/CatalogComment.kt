package com.mrboomdev.awery.ext.data

class CatalogComment(
	val avatar: String? = null,
	val name: String,
	val message: String,
	val flairs: Array<String>? = null,
	val likes: Int? = null,
	val dislikes: Int? = null,
	val votes: Int? = null,
	val repliesCount: Int? = null
)