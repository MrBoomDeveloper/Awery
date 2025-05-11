package com.mrboomdev.awery.ext.util

data class Progress(
	var max: Long = 0,
	var value: Long = 0
) {
	fun increment() {
		value++
	}

	val isCompleted: Boolean
		get() = value >= max

	fun finish() {
		value = max
	}
}