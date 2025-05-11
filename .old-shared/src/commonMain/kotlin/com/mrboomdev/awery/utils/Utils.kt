package com.mrboomdev.awery.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <I> I?.runOnNull(block: () -> Unit): I? {
	contract { 
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
	
	if(this == null) block()
	return this
}

/**
 * @param block You HAVE to use an ```return``` statement inside or else an exception would be raised.
 * It guarantees an null safety and so on...
 */
@OptIn(ExperimentalContracts::class)
inline fun <I> I?.returnOnNull(block: () -> Unit): I {
	contract {
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
	
	if(this == null) {
		block()
		throw IllegalStateException("You've supposed to call an return statement here!")
	}
	
	return this
}

/**
 * Will continue execution after you return true
 */
inline fun await(stopWaiting: () -> Boolean) {
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