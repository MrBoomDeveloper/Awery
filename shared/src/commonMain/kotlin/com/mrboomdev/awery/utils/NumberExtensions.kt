package com.mrboomdev.awery.utils

import kotlin.math.round
import kotlin.math.roundToLong

operator fun Number.compareTo(other: Number) = when(this) {
	is Int -> compareTo(other.toInt())
	is Float -> compareTo(other.toFloat())
	is Double -> compareTo(other.toDouble())
	is Long -> compareTo(other.toLong())
	else -> throw UnsupportedOperationException("Unsupported number type!")
}

fun Float.toStrippedString() = if(round(this) == this) {
	roundToLong().toString()
} else toString()