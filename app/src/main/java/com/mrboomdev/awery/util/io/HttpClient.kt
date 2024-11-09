package com.mrboomdev.awery.util.io

import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.app.data.Constants
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.async.AsyncUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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

	val client: OkHttpClient by lazy {
		OkHttpClient.Builder().apply {
			cache(File(appContext.cacheDir, Constants.DIRECTORY_NET_CACHE).let {
				Cache(it, 10 * 1024 * 1024 /* 10mb */)
			})

			if(AwerySettings.LOG_NETWORK.value) {
				addNetworkInterceptor(HttpLoggingInterceptor().apply {
					level = HttpLoggingInterceptor.Level.BODY
				})
			}
		}.build()
	}

	suspend fun HttpRequest.download(targetFile: File): File {
		val url = URL(url)

		return withContext(Dispatchers.IO) {
			targetFile.parentFile!!.mkdirs()
			targetFile.delete()
			targetFile.createNewFile()

			val connection = url.openConnection()

			for((key, value) in headers) {
				connection.setRequestProperty(key, value)
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

				for((key, value) in request.headers) {
					connection.setRequestProperty(key, value)
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
		val okRequest = Request.Builder()
		okRequest.url(request.url)

		for((key, value) in request.headers) {
			okRequest.addHeader(key, value)
		}

		when(request.method) {
			HttpMethod.GET -> okRequest.get()
			HttpMethod.HEAD -> okRequest.head()
			HttpMethod.DELETE -> okRequest.delete()

			else -> okRequest.method(request.method.name,
				request.body?.toRequestBody(request.mediaType?.toMediaTypeOrNull()))
		}

		if(request.cacheMode?.doCache == true) {
			okRequest.cacheControl(
				CacheControl.Builder()
					.onlyIfCached()
					.maxAge(request.cacheDuration, TimeUnit.SECONDS)
					.build()
			)
		}

		return executeCall(okRequest, request.cacheMode)
	}

	suspend fun HttpRequest.fetch(): HttpResponse {
		return withContext(Dispatchers.IO) {
			val okRequest = Request.Builder()
			okRequest.url(url)

			for((key, value) in headers) {
				okRequest.addHeader(key, value)
			}

			when(method) {
				HttpMethod.GET -> okRequest.get()
				HttpMethod.HEAD -> okRequest.head()
				HttpMethod.DELETE -> okRequest.delete()

				else -> okRequest.method(method.name,
					body?.toRequestBody(mediaType?.toMediaTypeOrNull()))
			}

			if(cacheMode?.doCache == true) {
				okRequest.cacheControl(
					CacheControl.Builder()
						.onlyIfCached()
						.maxAge(cacheDuration, TimeUnit.SECONDS)
						.build()
				)
			}

			executeCall(okRequest, cacheMode)
		}
	}

	private fun executeCall(okRequest: Request.Builder, mode: HttpCacheMode?): HttpResponse {
		client.newCall(okRequest.build()).execute().use { response ->
			if(mode != null && mode.doCache && response.code == 504) {
				val cacheControl = CacheControl.Builder().noCache().build()
				return executeCall(okRequest.cacheControl(cacheControl), HttpCacheMode.NETWORK_ONLY)
			}

			return HttpResponseImpl(response)
		}
	}

	private class HttpResponseImpl(response: Response) : HttpResponse() {
		override val text = response.body.string()
		override val statusCode = response.code
	}
}

class HttpRequest(val url: String) {
	val headers = mutableMapOf<String, String>()
	var mediaType: String? = null
	var cacheMode: HttpCacheMode? = null
	var method = HttpMethod.GET
	var body: String? = null

	/**
	 * Cache duration in seconds.
	 */
	var cacheDuration = 0
}

abstract class HttpResponse {
	abstract val text: String
	abstract val statusCode: Int

	override fun toString(): String {
		return """
				{
					"text": "__TEXT__",
					"statusCode": __STATUS_CODE__
				}
				
				"""
			.trimIndent()
			.replace("__TEXT__", text)
			.replace("__STATUS_CODE__", statusCode.toString())
	}
}

enum class HttpCacheMode {
	NETWORK_ONLY {
		override val doCache = false
	},

	CACHE_FIRST {
		override val doCache = true
	};

	abstract val doCache: Boolean
}

enum class HttpMethod {
	POST {
		override val doSendData = true
	},

	PUT {
		override val doSendData = true
	},

	PATCH {
		override val doSendData = true
	},

	DELETE {
		override val doSendData = false
	},

	HEAD {
		override val doSendData = false
	},

	GET {
		override val doSendData = false
	};

	abstract val doSendData: Boolean
}