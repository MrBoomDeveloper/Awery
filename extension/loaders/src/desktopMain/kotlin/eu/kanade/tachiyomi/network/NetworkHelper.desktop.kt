package eu.kanade.tachiyomi.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.net.CookieHandler
import java.net.HttpCookie

actual class PlatformCookieJar : CookieJar {
    private val cookieManager = CookieHandler.getDefault()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookieManager != null) {
            val uri = url.toUri()
            val headers = mutableMapOf<String, List<String>>()
            cookies.forEach { cookie ->
                headers.merge("Set-Cookie", listOf(cookie.toString())) { a, b -> a + b }
            }
            cookieManager.put(uri, headers)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        if (cookieManager != null) {
            val uri = url.toUri()
            val headers = cookieManager.get(uri, emptyMap())
            return headers["Cookie"]?.flatMap { headerValue ->
                headerValue.split(";").mapNotNull { cookieString ->
                    HttpCookie.parse(cookieString).firstOrNull()?.let { httpCookie ->
                        Cookie.Builder()
                            .name(httpCookie.name)
                            .value(httpCookie.value)
                            .domain(httpCookie.domain ?: url.host)
                            .build()
                    }
                }
            } ?: emptyList()
        }
        return emptyList()
    }
}