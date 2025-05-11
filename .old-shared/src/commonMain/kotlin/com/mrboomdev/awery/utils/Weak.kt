package com.mrboomdev.awery.utils

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class Weak<T>(
	val factory: () -> T
) {
	private var _value: WeakReference<T>? = null
	
	val value: T
		get() = _value?.get() ?: factory().also { 
			_value = WeakReference(it)
		}
}

fun <T> Weak<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
	return value
}