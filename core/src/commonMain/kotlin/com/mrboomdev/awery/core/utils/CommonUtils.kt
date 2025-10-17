package com.mrboomdev.awery.core.utils

import java.lang.AutoCloseable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    
    var exception: Throwable? = null
    try {
        return block(this)
    } catch(e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

@PublishedApi
internal fun AutoCloseable?.closeFinally(cause: Throwable?): Unit = when {
    this == null -> {}
    cause == null -> close()
    else ->
        try {
            close()
        } catch(closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}

inline fun <T> T.runIfNull(block: () -> Unit): T {
    return apply {
        if(this == null) {
            block()
        }
    }
}

/**
 * Blocks the current thread until the provided [block] function returns `true`.
 *
 * @param block A lambda function that returns a [Boolean]. The function will be repeatedly
 *              invoked until it returns `true`.
 */
inline fun await(block: () -> Boolean) {
    @Suppress("ControlFlowWithEmptyBody")
    while(!block()) {}
}

inline fun <T> retryUntilSuccess(onFailure: (Throwable) -> Unit = {}, block: () -> T): T {
    var result: T? = null

    while(result == null) {
        try {
            result = block()
        } catch(t: Throwable) {
            onFailure(t)
        }
    }

    return result
}

inline fun <T> tryOr(computeValue: () -> T, fallbackComputeValue: (Throwable) -> T): T {
    return try {
        computeValue()
    } catch(t: Throwable) {
        fallbackComputeValue(t)
    }
}