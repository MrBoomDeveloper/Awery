package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.utils.ExtensionSdk
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object NetworkHelper {
    @ExtensionSdk
    val client by lazy {
        OkHttpClient.Builder().apply {
            cookieJar(PlatformCookieJar)
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            callTimeout(2, TimeUnit.MINUTES)
            addInterceptor(BrotliInterceptor)
            
            cache(Cache(
                directory = Platform.CACHE_DIRECTORY,
                maxSize = 5L * 1024 * 1024, /* 5 MiB */))
            
            for(interceptor in interceptors) {
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
     * Stays here just form compatibility.
     * @deprecated Since extension-lib 1.5
     */
    @Deprecated("The regular client handles Cloudflare by default")
    @ExtensionSdk
    val cloudflareClient: OkHttpClient = client
    
    @ExtensionSdk
    fun defaultUserAgentProvider(): String = Platform.USER_AGENT
}

expect val NetworkHelper.interceptors: List<okhttp3.Interceptor>