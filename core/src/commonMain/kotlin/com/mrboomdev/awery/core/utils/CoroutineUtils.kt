package com.mrboomdev.awery.core.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

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

fun CoroutineScope.launchSupervise(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(context, start) {
    supervisorScope(block)
}

/**
 * Useful for view models.
 */
fun CoroutineScope.launchTryingSupervise(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCatch: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> Unit
) = launchTrying(context, start, onCatch) {
    supervisorScope(block)
}

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
 * Useful for view models.
 */
fun <T> CoroutineScope.asyncTryingSupervise(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCatch: (Throwable) -> Unit,
    block: suspend CoroutineScope.() -> T
) = asyncTrying(context, start, onCatch) {
    supervisorScope(block)
}

suspend inline fun <T> runForAtLeast(
	block: () -> T,
	durationInMillis: Long
): T {
	val (result, executionDuration) = measureTimedValue(block)
	
	if(executionDuration >= durationInMillis.milliseconds) {
		delay(durationInMillis.milliseconds - executionDuration)
	}
	
	return result
}