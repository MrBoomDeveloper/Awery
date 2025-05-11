package eu.kanade.tachiyomi.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

actual object PlatformCookieJar: CookieJar {
	override fun loadForRequest(url: HttpUrl): List<Cookie> {
		TODO("Not yet implemented")
	}
	
	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
		TODO("Not yet implemented")
	}
	
	actual suspend fun clear() {
		TODO()
	}
}