package com.mrboomdev.awery.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun Modifier.scaleX(x: Float) =
    scale(scaleX = x, scaleY = 1f)

fun Modifier.scaleY(y: Float) =
    scale(scaleX = 1f, scaleY = y)

@OptIn(ExperimentalContracts::class)
inline fun Modifier.thenScope(scope: Modifier.() -> Modifier): Modifier {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    val result = scope()
    return if(result == Modifier) this else result
}

@OptIn(ExperimentalContracts::class)
inline fun Modifier.thenIf(
    run: Boolean,
    scope: Modifier.() -> Modifier
): Modifier {
    contract {
        callsInPlace(scope, InvocationKind.AT_MOST_ONCE)
    }

    val result = if(run) scope() else this
    return if(result == Modifier) this else result
}

@OptIn(ExperimentalContracts::class)
inline fun Modifier.thenIfElse(
    run: Boolean,
    scopeIf: Modifier.() -> Modifier,
    scopeElse: Modifier.() -> Modifier
): Modifier {
    contract {
        callsInPlace(scopeIf, InvocationKind.AT_MOST_ONCE)
        callsInPlace(scopeElse, InvocationKind.AT_MOST_ONCE)
    }

    val result = if(run) scopeIf() else scopeElse()
    return if(result == Modifier) this else result
}