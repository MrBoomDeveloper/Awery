package com.mrboomdev.awery.util.extensions

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import rx.Observable
import rx.Subscriber
import rx.Subscription
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Stolen from Dantotsu 😎
suspend fun <T> Observable<T>.awaitSingle(): T = single().awaitOne()

// Stolen from Dantotsu 😎
@OptIn(InternalCoroutinesApi::class)
private suspend fun <T> Observable<T>.awaitOne(): T = suspendCancellableCoroutine { coroutine ->
	coroutine.unsubscribeOnCancellation(subscribe(object : Subscriber<T>() {
		override fun onCompleted() {
			if(!coroutine.isActive) return
			coroutine.resumeWithException(IllegalStateException("Should have been invoked onNext()"))
		}

		override fun onError(e: Throwable) {
			coroutine.tryResumeWithException(e)?.let { token ->
				coroutine.completeResume(token)
			}
		}

		override fun onNext(t: T) {
			coroutine.resume(t)
		}

		override fun onStart() {
			request(1)
		}
	}))
}

// Stolen from Dantotsu 😎
private fun <T> CancellableContinuation<T>.unsubscribeOnCancellation(
	subscription: Subscription
) = invokeOnCancellation { subscription.unsubscribe() }