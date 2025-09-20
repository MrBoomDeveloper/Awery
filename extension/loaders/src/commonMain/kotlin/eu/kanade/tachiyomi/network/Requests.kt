@file:Suppress("FunctionName")

package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.core.utils.PlatformSdk
import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit.MINUTES

@PlatformSdk
private val DEFAULT_CACHE_CONTROL = CacheControl.Builder().maxAge(10, MINUTES).build()

@PlatformSdk
private val DEFAULT_HEADERS = Headers.Builder().build()

@PlatformSdk
private val DEFAULT_BODY: RequestBody = FormBody.Builder().build()

@PlatformSdk
fun GET(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    cache: CacheControl = DEFAULT_CACHE_CONTROL,
): Request {
    return GET(url.toHttpUrl(), headers, cache)
}

/**
 * @since extensions-lib 1.4
 */
@PlatformSdk
fun GET(
    url: HttpUrl,
    headers: Headers = DEFAULT_HEADERS,
    cache: CacheControl = DEFAULT_CACHE_CONTROL,
): Request {
    return Request.Builder()
        .url(url)
        .headers(headers)
        .cacheControl(cache)
        .build()
}

@PlatformSdk
fun POST(
    url: String,
    headers: Headers = DEFAULT_HEADERS,
    body: RequestBody = DEFAULT_BODY,
    cache: CacheControl = DEFAULT_CACHE_CONTROL,
): Request {
    return Request.Builder()
        .url(url)
        .post(body)
        .headers(headers)
        .cacheControl(cache)
        .build()
}