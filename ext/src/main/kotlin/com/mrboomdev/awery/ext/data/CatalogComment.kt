package com.mrboomdev.awery.ext.data

import com.mrboomdev.awery.ext.constants.AweryFeature

class CatalogComment(
	val avatar: String? = null,
	val name: String,
	val message: String,
	val flairs: Array<String>? = null,
	val likes: Int? = null,
	val dislikes: Int? = null,
	val votes: Int? = null,
	val features: Array<AweryFeature>? = null,
	val repliesCount: Int? = null
)