package com.mrboomdev.awery.utils

fun <E> List<E>.limit(max: Int): List<E> {
	if(max == 0) {
		return emptyList()
	}
	
	if(max >= size) {
		return toList()
	}
	
	return buildList(max) {
		for(item in this) {
			add(item)
			if(size >= max) break
		}
	}
}

/**
 * Fills an list with nulls until it matches an requested size.
 */
fun <E> MutableList<E?>.ensureSize(size: Int) {
	while(this.size < size) {
		add(null)
	}
}

inline fun <reified T> arrayOfNotNull(vararg items: T) = 
	arrayOf(*items).filterNotNull().toTypedArray()

/**
 * Only pairs with both key and value specified
 * will be permitted to the returned map.
 */
fun <K, V> mapOfNotNull(vararg pairs: Pair<K?, V?>): Map<K, V> {
	return mutableMapOf<K, V>().apply {
		for((key, value) in pairs) {
			if(key != null && value != null) {
				put(key, value)
			}
		}
	}.toMap()
}

fun <K, V> buildNotNullMap(action: NotNullMapBuilder<K, V>.() -> Unit) = buildMap<K, V> { 
	object : NotNullMapBuilder<K, V> {
		override fun put(key: K?, value: V?) {
			if(key != null && value != null) {
				this@buildMap.put(key, value)
			}
		}
	}.apply(action)
}

interface NotNullMapBuilder<K, V> {
	/**
	 * If no key or no value was set, then the output won't change.
	 */
	fun put(key: K?, value: V?)
}