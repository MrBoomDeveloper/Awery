package com.mrboomdev.awery.util

import java.lang.ref.WeakReference

/**
 * The actual value may be cleared at any time by the gc if there's no references left out in the whole process.
 * Then the specified initializer would be called again to restore the value.
 */
class WeakLazy<T>(val initializer: () -> T): Lazy<T> {
	private var weakReference: WeakReference<T>? = null

	private fun initValue(): T {
		val value = initializer()
		weakReference = WeakReference(value)
		return value
	}

	override val value: T
		get() = weakReference?.get() ?: initValue()

	override fun isInitialized(): Boolean {
		return weakReference?.get() != null
	}
}