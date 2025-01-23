package com.mrboomdev.awery.utils

inline fun <T> tryOr(action: () -> T, or: (Throwable) -> T) = try {
	action()
} catch(t: Throwable) {
	or(t)
}

inline fun <T> tryOrNull(action: () -> T) = try {
	action()
} catch(_: Throwable) { null }