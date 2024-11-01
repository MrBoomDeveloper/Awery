package com.mrboomdev.awery.util.extensions

import java.util.Arrays

/**
 * Only pairs with both key and value specified
 * will be permitted to the returned map.
 */
fun <K, V> mapOfNotNull(vararg pairs: Pair<K?, V?>): Map<K, V> {
	return LinkedHashMap<K, V>().apply {
		for((key, value) in pairs) {
			if(key != null && value != null) {
				put(key, value)
			}
		}
	}
}

fun <E> List<E>.limit(max: Int): List<E> {
	if(max == 0) {
		return emptyList()
	}

	val list = if(size > max) {
		ArrayList<E>(max)
	} else {
		return ArrayList(this)
	}

	var index = 0

	for(item in this) {
		list.add(item)

		index++

		if(index >= max) {
			break
		}
	}

	return list
}