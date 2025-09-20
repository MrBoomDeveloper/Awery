package com.mrboomdev.awery.core.utils

sealed class NumberPool<T>(
	private val range: ClosedRange<T>
) where T: Number, T: Comparable<T> {
	fun next(): T {
		if(current >= range.endInclusive) {
			current = range.start
		}
		
		val result = current
		increment()
		return result
	}
	
	protected var current = range.start
	protected abstract fun increment()
}

class IntPool(
	range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE
): NumberPool<Int>(range) {
	override fun increment() {
		current++
	}
}