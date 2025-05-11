package mihonx.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.Semaphore
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun OkHttpClient.Builder.rateLimit(
	permits: Int,
	period: Duration = 1.seconds,
	shouldLimit: (HttpUrl) -> Boolean
) = object : Interceptor {
	
	private val requestQueue = ArrayDeque<Long>(permits)
	private val rateLimitMillis = period.inWholeMilliseconds
	private val fairLock = Semaphore(1, true)
	
	override fun intercept(chain: Interceptor.Chain): Response {
		val call = chain.call()
		if(call.isCanceled()) throw IOException("Canceled")
		
		val request = chain.request()
		
		if(!shouldLimit(request.url)) {
			return chain.proceed(request)
		}
		
		try {
			fairLock.acquire()
		} catch(e: InterruptedException) {
			throw IOException(e)
		}
		
		val requestQueue = this.requestQueue
		val timestamp: Long
		
		try {
			synchronized(requestQueue) {
				while(requestQueue.size >= permits) { // queue is full, remove expired entries
					val periodStart = System.currentTimeMillis() - rateLimitMillis
					var hasRemovedExpired = false
					
					while(requestQueue.isEmpty().not() && requestQueue.first <= periodStart) {
						requestQueue.removeFirst()
						hasRemovedExpired = true
					}
					
					if(call.isCanceled()) {
						throw IOException("Canceled")
					} else if(hasRemovedExpired) break else {
						try { // wait for the first entry to expire, or notified by cached response
							(requestQueue as Object).wait(requestQueue.first - periodStart)
						} catch(_: InterruptedException) {
							continue
						}
					}
				}
				
				// add request to queue
				timestamp = System.currentTimeMillis()
				requestQueue.addLast(timestamp)
			}
		} finally {
			fairLock.release()
		}
		
		val response = chain.proceed(request)
		if(response.networkResponse == null) { // response is cached, remove it from queue
			synchronized(requestQueue) {
				if(requestQueue.isEmpty() || timestamp < requestQueue.first) return@synchronized
				requestQueue.removeFirstOccurrence(timestamp)
				
				@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
				(requestQueue as Object).notifyAll()
			}
		}
		
		return response
	}
}