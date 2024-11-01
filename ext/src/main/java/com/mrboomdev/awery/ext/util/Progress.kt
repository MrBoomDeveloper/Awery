package com.mrboomdev.awery.ext.util

class Progress @JvmOverloads constructor(
	var max: Long = 0,
	var progress: Long = 0
) {
	fun increment() {
		progress++
	}

	var isCompleted: Boolean
		get() = progress >= max
		set(value) {
			if(value) {
				progress = max
			} else if(progress >= max) {
				progress = 0
			}
		}
}