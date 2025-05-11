package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.platform.Platform
import eu.kanade.tachiyomi.network.interceptor.CloudflareInterceptor
import eu.kanade.tachiyomi.network.interceptor.UncaughtExceptionInterceptor
import eu.kanade.tachiyomi.network.interceptor.UserAgentInterceptor
import okhttp3.Interceptor

actual val NetworkHelper.interceptors: List<Interceptor>
	get() = listOf(
		UncaughtExceptionInterceptor(),
		UserAgentInterceptor(::defaultUserAgentProvider),
		CloudflareInterceptor(Platform, PlatformCookieJar, ::defaultUserAgentProvider)
	)