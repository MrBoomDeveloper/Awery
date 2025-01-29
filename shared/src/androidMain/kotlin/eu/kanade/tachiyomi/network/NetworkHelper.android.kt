package eu.kanade.tachiyomi.network

import okhttp3.CookieJar
import okhttp3.Interceptor

actual val NetworkHelper.platformInterceptors: List<Interceptor>
	get() = listOf(
		UncaughtExceptionInterceptor(),
		UserAgentInterceptor(::defaultUserAgentProvider),
		CloudflareInterceptor(platformCookieJar, this::defaultUserAgentProvider)
	)