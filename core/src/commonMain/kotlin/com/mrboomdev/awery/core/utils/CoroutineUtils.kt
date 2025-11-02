package com.mrboomdev.awery.core.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Launches a new coroutine in the [GlobalScope] without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * This function is similar to [kotlinx.coroutines.launch] but specifically uses [GlobalScope].
 * It's generally recommended to use a [CoroutineScope] that is tied to a specific lifecycle (e.g., an Activity or ViewModel scope)
 * to avoid memory leaks and ensure proper cancellation. However, this function can be useful for tasks that need to run
 * independently of any specific lifecycle.
 *
 * @param context additional to [CoroutineScope.coroutineContext] context of the coroutine.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 * @return A [Job] representing the launched coroutine.
 */
fun launchGlobal(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    @OptIn(DelicateCoroutinesApi::class)
    return GlobalScope.launch(context, start, block)
}

/**
 * Launches a new coroutine in the current scope and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * If the coroutine throws an exception, it will be caught and passed to the [onCatch] function. The function will be called
 *        with the exception as its argument.
 *
 * @param context additional to [CoroutineScope.coroutineContext] context of the coroutine.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param onCatch the function to call when the coroutine throws an exception. The function will be called
 *        with the exception as its argument.
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 * @return A [Job] representing the launched coroutine.
 */
fun CoroutineScope.launchTrying(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCatch: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> Unit
) = launch(
    context = context + CoroutineExceptionHandler { _, t -> onCatch(t) },
    start = start,
    block = block
)

/**
 * Launches a new coroutine in the current scope that will be supervised by the [supervisorScope] function.
 * This means that if the coroutine throws an exception, it will be propagated to the parent coroutine and
 * will not cancel the parent coroutine.
 *
 * @param context additional to [CoroutineScope.coroutineContext] context of the coroutine.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 * @return A [Job] representing the launched coroutine.
 */
fun CoroutineScope.launchSupervise(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(context, start) {
    supervisorScope(block)
}

/**
 * A combination of [launchTrying] and [supervisorScope] that launches a coroutine in the current scope and
 * returns its result as a [kotlinx.coroutines.Deferred] value. If the coroutine throws an exception, it will be caught and
 * passed to the [onCatch] function.
 *
 * This function is useful for view models that need to handle exceptions in a centralized way.
 *
 * @param context additional to [CoroutineScope.coroutineContext] context of the coroutine.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param onCatch the function to call when the coroutine throws an exception. The function will be called
 *        with the exception as its argument.
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 * @return A [kotlinx.coroutines.Deferred] representing the launched coroutine.
 */
fun CoroutineScope.launchTryingSupervise(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCatch: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> Unit
) = launchTrying(context, start, onCatch) {
    supervisorScope(block)
}

/**
 * Launches a coroutine that will be cancelled when the resulting [kotlinx.coroutines.Deferred] is cancelled.
 * The coroutine is launched in the current scope and returns its result as a [kotlinx.coroutines.Deferred] value.
 * If the coroutine throws an exception, it will be caught and passed to the [onCatch] function.
 *
 * This function is useful for view models that need to handle exceptions in a centralized way.
 *
 * @param context additional to [CoroutineScope.coroutineContext] context of the coroutine.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param onCatch the function to call when the coroutine throws an exception. The function will be called
 *        with the exception as its argument.
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 * @return A [kotlinx.coroutines.Deferred] representing the launched coroutine.
 */
fun <T> CoroutineScope.asyncTrying(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCatch: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> T
) = async(
    context = context + CoroutineExceptionHandler { _, t -> onCatch(t) },
    start = start,
    block = block
)

/**
 * A combination of [asyncTrying] and [supervisorScope] that launches a coroutine in the current scope and
 * returns its result as a [kotlinx.coroutines.Deferred] value. If the coroutine throws an exception, it will be caught and
 * passed to the [onCatch] function.
 *
 * This function is useful for view models that need to handle exceptions in a centralized way.
 *
 * @param context additional to [CoroutineScope.coroutineContext] context of the coroutine.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param onCatch the function to call when the coroutine throws an exception. The function will be called
 *        with the exception as its argument.
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 * @return A [kotlinx.coroutines.Deferred] representing the launched coroutine.
 */
fun <T> CoroutineScope.asyncTryingSupervise(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCatch: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> T
) = asyncTrying(context, start, onCatch) {
    supervisorScope(block)
}