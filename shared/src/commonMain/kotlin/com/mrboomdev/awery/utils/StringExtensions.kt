package com.mrboomdev.awery.utils

fun <T: Enum<T>> String.toEnum(clazz: Class<T>): T? {
	return try {
		java.lang.Enum.valueOf(clazz, this)
	} catch(e: IllegalArgumentException) { null }
}

inline fun <reified T: Enum<T>> String.toEnum(): T? {
	return toEnum(T::class.java)
}