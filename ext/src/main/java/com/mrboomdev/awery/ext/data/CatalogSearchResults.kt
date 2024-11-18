package com.mrboomdev.awery.ext.data

import java.util.LinkedList

class CatalogSearchResults<E>(
	items: Collection<E>,
	val hasNextPage: Boolean = false
): LinkedList<E>(items)