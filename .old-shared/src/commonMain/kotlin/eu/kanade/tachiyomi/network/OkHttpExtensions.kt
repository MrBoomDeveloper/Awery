package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.utils.ExtensionSdk
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.Producer
import rx.Subscription
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resumeWithException

fun OkHttpClient.newCachelessCallWithProgress(request: Request, listener: ProgressListener): Call {
    val progressClient = newBuilder()
        .cache(null)
        .addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body, listener))
                .build()
        }.build()
    
    return progressClient.newCall(request)
}

@ExtensionSdk
fun Call.asObservable(): Observable<Response> {
    return Observable.unsafeCreate { subscriber ->
        // Since Call is a one-shot type, clone it for each new subscriber.
        val call = clone()

        // Wrap the call in a helper which handles both unsubscription and backpressure.
        val requestArbiter = object : AtomicBoolean(), Producer, Subscription {
            override fun request(n: Long) {
                if(n == 0L || !compareAndSet(false, true)) return

                try {
                    val response = call.execute()
                    
                    if(!subscriber.isUnsubscribed) {
                        subscriber.onNext(response)
                        subscriber.onCompleted()
                    }
                } catch(e: Exception) {
                    if(!subscriber.isUnsubscribed) {
                        subscriber.onError(e)
                    }
                }
            }

            override fun unsubscribe() {
                call.cancel()
            }

            override fun isUnsubscribed(): Boolean {
                return call.isCanceled()
            }
        }

        subscriber.add(requestArbiter)
        subscriber.setProducer(requestArbiter)
    }
}

// Based on https://github.com/gildor/kotlin-coroutines-okhttp
@ExtensionSdk
private suspend fun Call.await(callStack: Array<StackTraceElement>): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response) { _, _, _ ->
                    response.body.close()
                }
            }
            
            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if(continuation.isCancelled) return
                continuation.resumeWithException(IOException(e.message, e).apply { stackTrace = callStack })
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch(_: Throwable) {
                // Ignore cancel exception
            }
        }
    }
}

@ExtensionSdk
suspend fun Call.await(): Response {
    val callStack = Exception().stackTrace.run { copyOfRange(1, size) }
    return await(callStack)
}

@ExtensionSdk
suspend fun Call.awaitSuccess(): Response {
    val callStack = Exception().stackTrace.run { copyOfRange(1, size) }
    val response = await(callStack)
    
    if(!response.isSuccessful) {
        response.close()
        throw HttpException(response.code).apply { stackTrace = callStack }
    }
    
    return response
}

@ExtensionSdk
fun Call.asObservableSuccess(): Observable<Response> {
    return asObservable().doOnNext { response ->
        if(!response.isSuccessful) {
            response.close()
            throw HttpException(response.code)
        }
    }
}

@ExtensionSdk
class HttpException(val code: Int) : IllegalStateException("HTTP error $code")