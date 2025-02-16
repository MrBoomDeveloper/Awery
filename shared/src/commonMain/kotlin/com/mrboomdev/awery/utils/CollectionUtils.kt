package com.mrboomdev.awery.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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