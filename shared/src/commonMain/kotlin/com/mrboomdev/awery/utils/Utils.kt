package com.mrboomdev.awery.utils

/**
 * Will continue execution after you return true
 */
inline fun await(crossinline stopWaiting: () -> Boolean) {
	@Suppress("ControlFlowWithEmptyBody")
	while(!stopWaiting()) {}
}

/**
 * Tries to get an result of at least one action and returns it or null if all actions has failed
 */
fun <T> tryOrTry(vararg actions: () -> T): T? {
	for(action in actions) {
		try {
			return action()
		} catch(_: Throwable) {}
	}
	
	return null
}

inline fun <T> tryOr(action: () -> T, or: (Throwable) -> T) = try {
	action()
} catch(t: Throwable) {
	or(t)
}

/**
 * Returns an result of the action or null if it has failed
 */
inline fun <T> tryOrNull(action: () -> T) = try {
	action()
} catch(_: Throwable) { null }