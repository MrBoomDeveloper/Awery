@file:OptIn(ExperimentalContracts::class)

package com.mrboomdev.awery.util.extensions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <I, O> I.letWith(callback: I.() -> O): O {
	contract {
		callsInPlace(callback, InvocationKind.EXACTLY_ONCE)
	}

	return callback()
}