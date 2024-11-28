package com.mrboomdev.awery.util.extensions

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import java.util.LinkedHashMap

@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "DEPRECATION")
inline fun <reified T> Bundle.get(key: String): T? {
	if(!containsKey(key)) {
		return null
	}

	return get(key) as T
}

@Suppress("DEPRECATION")
inline fun <reified T> Intent.get(key: String): T? {
	if(!hasExtra(key)) {
		return null
	}

	// We may get the value by an optimized way without copying the bundle
	when(T::class) {
		String::class -> return getStringExtra(key) as? T
		CharSequence::class -> return getCharSequenceExtra(key) as? T
		Char::class -> return getCharExtra(key, '0') as? T

		Boolean::class -> return getBooleanExtra(key, false) as? T
		Byte::class -> return getByteExtra(key, 0) as? T
		Short::class -> return getShortExtra(key, 0) as? T
		Int::class -> return getIntExtra(key, 0) as? T
		Long::class -> return getLongExtra(key, 0) as? T
		Float::class -> return getFloatExtra(key, 0f) as? T
		Double::class -> return getDoubleExtra(key, 0.0) as? T

		Array<Parcelable>::class -> return getParcelableArrayExtra(key) as? T
		Array<CharSequence>::class -> return getCharSequenceArrayExtra(key) as? T
		Array<Char>::class -> return getCharArrayExtra(key) as? T
		Array<String>::class -> return getStringArrayExtra(key) as? T
		Array<Boolean>::class -> return getBooleanArrayExtra(key) as? T
		Array<Byte>::class -> return getByteArrayExtra(key) as? T
		Array<Short>::class -> return getShortArrayExtra(key) as? T
		Array<Int>::class -> return getIntArrayExtra(key) as? T
		Array<Long>::class -> return getLongArrayExtra(key) as? T
		Array<Float>::class -> return getFloatArrayExtra(key) as? T
		Array<Double>::class -> return getDoubleArrayExtra(key) as? T

		Serializable::class -> return getSerializableExtra(key) as? T
		Parcelable::class -> return getParcelableExtra(key) as? T
		Bundle::class -> return getBundleExtra(key) as? T
	}

	// We have to rely on using this heavy method
	return extras?.get<T>(key)
}

@Suppress("UNCHECKED_CAST")
fun Intent.put(key: String, value: Any?) {
	if(value == null) {
		removeExtra(key)
		return
	}

	if(value is String) {
		putExtra(key, value)
		return
	}

	if(value is CharSequence) {
		putExtra(key, value)
		return
	}

	if(value is Char) {
		putExtra(key, value)
		return
	}

	if(value is Boolean) {
		putExtra(key, value)
		return
	}

	if(value is Byte) {
		putExtra(key, value)
		return
	}

	if(value is Int) {
		putExtra(key, value)
		return
	}

	if(value is Double) {
		putExtra(key, value)
		return
	}

	if(value is Float) {
		putExtra(key, value)
		return
	}

	if(value is Long) {
		putExtra(key, value)
		return
	}

	if(value is Bundle) {
		putExtra(key, value)
		return
	}

	if(value is Array<*> && value.isArrayOf<String>()) {
		putExtra(key, value as Array<String>)
		return
	}

	if(value is Array<*> && value.isArrayOf<Int>()) {
		putExtra(key, value as Array<Int>)
		return
	}

	if(value is Array<*> && value.isArrayOf<Boolean>()) {
		putExtra(key, value as Array<Boolean>)
		return
	}

	if(value is Array<*> && value.isArrayOf<Long>()) {
		putExtra(key, value as Array<Long>)
		return
	}

	if(value is Array<*> && value.isArrayOf<Float>()) {
		putExtra(key, value as Array<Float>)
		return
	}

	if(value is Array<*> && value.isArrayOf<Double>()) {
		putExtra(key, value as Array<Double>)
		return
	}

	if(value is Array<*> && value.isArrayOf<Serializable>()) {
		putExtra(key, value as Array<Serializable>)
		return
	}

	if(value is Array<*> && value.isArrayOf<Parcelable>()) {
		putExtra(key, value as Array<Parcelable>)
		return
	}

	if(value is Serializable) {
		putExtra(key, value)
		return
	}

	if(value is Parcelable) {
		putExtra(key, value)
		return
	}

	throw UnsupportedOperationException("Unsupported value type: ${value::class.simpleName}")
}