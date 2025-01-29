package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.platform.Platform
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object NetworkHelper {
    val client by lazy {
        OkHttpClient.Builder().apply {
            cookieJar(platformCookieJar)
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            callTimeout(2, TimeUnit.MINUTES)
            addInterceptor(BrotliInterceptor)
            
            cache(Cache(
                directory = Platform.CACHE_DIRECTORY,
                maxSize = 5L * 1024 * 1024, /* 5 MiB */))
            
            for(interceptor in platformInterceptors) {
                addInterceptor(interceptor)
            }
            
            if(AwerySettings.LOG_NETWORK.value) {
                addNetworkInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }.build()
    }
    
    /**
     * @deprecated Since extension-lib 1.5
     */
    @Deprecated("The regular client handles Cloudflare by default")
    @Suppress("UNUSED")
    val cloudflareClient: OkHttpClient = client

    fun defaultUserAgentProvider(): String = platformUserAgent
}

expect val NetworkHelper.platformUserAgent: String
expect val NetworkHelper.platformInterceptors: List<okhttp3.Interceptor>