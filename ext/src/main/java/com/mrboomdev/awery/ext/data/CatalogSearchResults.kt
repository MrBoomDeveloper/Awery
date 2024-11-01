package com.mrboomdev.awery.ext.data

class CatalogSearchResults<T>(
	val items: List<T>,
	val hasNextPage: Boolean = false
)