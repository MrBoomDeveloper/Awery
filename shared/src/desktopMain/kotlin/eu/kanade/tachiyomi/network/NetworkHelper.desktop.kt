package eu.kanade.tachiyomi.network

import okhttp3.Interceptor

actual val NetworkHelper.interceptors: List<Interceptor>
	get() = emptyList()