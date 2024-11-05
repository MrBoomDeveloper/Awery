@file:OptIn(ExperimentalContracts::class)

package com.mrboomdev.awery.util.extensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <I, O> I.applyCopy(callback: I.() -> O): O {
	contract {
		callsInPlace(callback, InvocationKind.EXACTLY_ONCE)
	}

	return callback()
}

inline fun <reified T, O> Any?.ifIs(callback: (casted: T) -> O): O? {
	contract {
		callsInPlace(callback, InvocationKind.AT_MOST_ONCE)
	}

	if(this is T) {
		return callback(this)
	}

	return null
}

inline fun <reified T> Any?.ifIs(callback: (casted: T) -> Unit) {
	contract {
		callsInPlace(callback, InvocationKind.AT_MOST_ONCE)
	}

	if(this is T) {
		callback(this)
	}
}