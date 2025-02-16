package eu.kanade.tachiyomi.network

import okhttp3.CookieJar

expect object PlatformCookieJar: CookieJar {
	suspend fun clear()
}