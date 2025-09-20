package eu.kanade.tachiyomi.network.interceptor

import eu.kanade.tachiyomi.network.PlatformCookieJar
import okhttp3.Interceptor
import okhttp3.Response

// TODO: Implement the whole thing by using desktop webviews
actual class CloudflareInterceptor actual constructor(
    private val cookieManager: PlatformCookieJar,
    private val defaultUserAgentProvider: () -> String
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}