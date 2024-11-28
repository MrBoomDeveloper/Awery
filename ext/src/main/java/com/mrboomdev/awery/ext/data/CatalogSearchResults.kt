package com.mrboomdev.awery.ext.data

import java.util.Collections
import java.util.LinkedList

class CatalogSearchResults<E>(
	items: Collection<E>,
	val hasNextPage: Boolean = false
): LinkedList<E>(items) {
	companion object {
		private val EMPTY = CatalogSearchResults<Any>(Collections.emptyList())

		@Suppress("UNCHECKED_CAST")
		fun <E> empty() = EMPTY as CatalogSearchResults<E>
	}
}