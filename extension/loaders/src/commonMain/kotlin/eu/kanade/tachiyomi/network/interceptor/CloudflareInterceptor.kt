package eu.kanade.tachiyomi.network.interceptor

import eu.kanade.tachiyomi.network.PlatformCookieJar
import okhttp3.Interceptor

expect class CloudflareInterceptor(
    cookieManager: PlatformCookieJar,
    defaultUserAgentProvider: () -> String
): Interceptor