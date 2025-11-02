package com.mrboomdev.awery.core.utils

import java.lang.AutoCloseable
import kotlin.Boolean
import kotlin.OptIn
import kotlin.PublishedApi
import kotlin.Suppress
import kotlin.Throwable
import kotlin.Unit
import kotlin.addSuppressed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Closes the given [AutoCloseable] instance after executing the given [block].
 * If [block] throws an exception, it will be re-thrown after closing the instance.
 * If the instance cannot be closed because of an exception, the original exception will be re-thrown.
 *
 * @param block A lambda function that takes a [T] as an argument and returns a result of type [R].
 * The function will be called with the instance of [T] as its argument and will be executed exactly once.
 * @return The result of executing [block] with the instance of [T] as its argument.
 */
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

/**
 * Closes this [AutoCloseable] instance, if it is not null.
 * If the provided [cause] is not null, it will be re-thrown after closing the instance.
 * If the instance cannot be closed because of an exception, the original exception will be re-thrown.
 * If the instance can be closed successfully, the provided [cause] will be re-thrown.
 *
 * @param cause The exception to be re-thrown after closing the instance.
 * @return Unit
 */
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

/**
 * Retries the provided [block] function until it returns a non-null result, or until an exception is thrown.
 * If an exception is thrown, the provided [onFailure] function will be called with the exception as an argument.
 * If [onFailure] is not provided, it defaults to an empty lambda function.
 *
 * @param onFailure A lambda function that takes a [Throwable] as an argument and returns [Unit].
 *                  The function will be called with the exception as an argument if an exception is thrown by [block].
 * @param block A lambda function that returns a result of type [T].
 *                  The function will be repeatedly invoked until it returns a non-null result.
 * @return The result of executing [block].
 */
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

/**
 * Tries to execute the provided [computeValue] block and returns the result if it doesn't throw an exception.
 * If the block throws an exception, the provided [fallbackComputeValue] block is executed with the exception as an argument
 * and the result of that block is returned.
 *
 * @param computeValue A lambda function that returns a value of type [T].
 * @param fallbackComputeValue A lambda function that takes a [Throwable] as an argument and returns a value of type [T].
 * @return The result of executing [computeValue] or [fallbackComputeValue] depending on whether an exception was thrown.
 */
inline fun <T> tryOr(computeValue: () -> T, fallbackComputeValue: (Throwable) -> T): T {
    return try {
        computeValue()
    } catch(t: Throwable) {
        fallbackComputeValue(t)
    }
}