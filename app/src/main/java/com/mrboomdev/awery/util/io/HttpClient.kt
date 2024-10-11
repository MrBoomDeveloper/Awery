package com.mrboomdev.awery.util.io

import com.mrboomdev.awery.app.AweryLifecycle
import com.mrboomdev.awery.data.Constants
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.async.AsyncUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels
import java.util.concurrent.TimeUnit

object HttpClient {
	@JvmStatic
	val client: OkHttpClient by lazy {
		val builder = OkHttpClient.Builder()

		val cacheDir = File(AweryLifecycle.getAppContext().cacheDir, Constants.DIRECTORY_NET_CACHE)
		val cache = Cache(cacheDir, 10 * 1024 * 1024  /* 10mb */)
		builder.cache(cache)

		if(AwerySettings.LOG_NETWORK.value) {
			val httpLoggingInterceptor = HttpLoggingInterceptor()
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
			builder.addNetworkInterceptor(httpLoggingInterceptor)
		}

		builder.build()
	}

	suspend fun HttpRequest.download(targetFile: File): File {
		checkFields()
		val url = URL(url)

		return withContext(Dispatchers.IO) {
			targetFile.parentFile!!.mkdirs()
			targetFile.delete()
			targetFile.createNewFile()

			val connection = url.openConnection()

			if(headers != null) {
				for((key, value) in headers) {
					connection.setRequestProperty(key, value)
				}
			}

			val httpChannel = Channels.newChannel(connection.getInputStream())

			FileOutputStream(targetFile).use {
				it.channel.transferFrom(httpChannel, 0, Long.MAX_VALUE)
			}

			targetFile
		}
	}

	@JvmStatic
	@Deprecated(message = "Will be removed after full migration to Kotlin")
	fun download(request: HttpRequest, targetFile: File): AsyncFuture<File> {
		val url: URL

		try {
			request.checkFields()
			url = URL(request.url)
		} catch(t: Throwable) {
			return AsyncUtils.futureFailNow(t)
		}

		return AsyncUtils.controllableFuture { future ->
			try {
				targetFile.parentFile!!.mkdirs()
				targetFile.delete()
				targetFile.createNewFile()

				val connection = url.openConnection()

				if(request.headers != null) {
					for((key, value) in request.headers) {
						connection.setRequestProperty(key, value)
					}
				}

				val httpChannel = Channels.newChannel(connection.getInputStream())

				FileOutputStream(targetFile).use { fos ->
					fos.channel.transferFrom(httpChannel, 0, Long.MAX_VALUE)
				}

				future.complete(targetFile)
			} catch(e: IOException) {
				future.fail(e)
			}
		}
	}

	@JvmStatic
	@Throws(IOException::class)
	@Deprecated(message = "Will be removed after full migration to Kotlin")
	fun fetchSync(request: HttpRequest): HttpResponse {
		request.checkFields()

		val okRequest = Request.Builder()
		okRequest.url(request.url)

		if(request.headers != null) {
			for((key, value) in request.headers) {
				okRequest.addHeader(key, value)
			}
		}

		when(request.method) {
			HttpMethod.GET -> okRequest.get()
			HttpMethod.HEAD -> okRequest.head()
			HttpMethod.DELETE -> okRequest.delete()
			else -> okRequest.method(
				request.method.name, if(request.form != null) request.form.build()
				else request.body.toRequestBody(request.mediaType)
			)
		}
		if(request.cacheMode != null && request.cacheMode.doCache()) {
			okRequest.cacheControl(
				CacheControl.Builder()
					.onlyIfCached()
					.maxAge(request.cacheDuration, TimeUnit.MILLISECONDS)
					.build()
			)
		}

		return executeCall(okRequest, request.cacheMode)
	}

	@JvmStatic
	@Deprecated(message = "Will be removed after full migration to Kotlin")
	fun fetch(request: HttpRequest): AsyncFuture<HttpResponse> {
		return AsyncUtils.thread<HttpResponse> { fetchSync(request) }
	}

	suspend fun HttpRequest.fetch(): HttpResponse {
		return withContext(Dispatchers.IO) {
			checkFields()

			val okRequest = Request.Builder()
			okRequest.url(url)

			if(headers != null) {
				for((key, value) in headers) {
					okRequest.addHeader(key, value)
				}
			}

			when(method) {
				HttpMethod.GET -> okRequest.get()
				HttpMethod.HEAD -> okRequest.head()
				HttpMethod.DELETE -> okRequest.delete()
				else -> okRequest.method(
					method.name, if(form != null) form.build()
					else body.toRequestBody(mediaType)
				)
			}
			if(cacheMode != null && cacheMode.doCache()) {
				okRequest.cacheControl(
					CacheControl.Builder()
						.onlyIfCached()
						.maxAge(cacheDuration, TimeUnit.MILLISECONDS)
						.build()
				)
			}

			executeCall(okRequest, cacheMode)
		}
	}

	@Throws(IOException::class)
	private fun executeCall(okRequest: Request.Builder, mode: HttpCacheMode?): HttpResponse {
		client.newCall(okRequest.build()).execute().use { response ->
			if(mode != null && mode.doCache() && response.code == 504) {
				val cacheControl = CacheControl.Builder().noCache().build()
				return executeCall(okRequest.cacheControl(cacheControl), HttpCacheMode.NETWORK_ONLY)
			}

			return HttpResponseImpl(response)
		}
	}

	private class HttpResponseImpl(response: Response) : HttpResponse() {
		private val text = response.body.string()
		private val code = response.code

		override fun getText(): String {
			return text
		}

		override fun getStatusCode(): Int {
			return code
		}
	}
}