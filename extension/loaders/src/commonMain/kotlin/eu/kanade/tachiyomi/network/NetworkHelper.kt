package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.PlatformSdk
import com.mrboomdev.awery.core.utils.toJavaFile
import eu.kanade.tachiyomi.network.interceptor.CloudflareInterceptor
import eu.kanade.tachiyomi.network.interceptor.IgnoreGzipInterceptor
import eu.kanade.tachiyomi.network.interceptor.UncaughtExceptionInterceptor
import eu.kanade.tachiyomi.network.interceptor.UserAgentInterceptor
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.resolve
import okhttp3.Cache
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import java.util.concurrent.TimeUnit

@PlatformSdk
object NetworkHelper {
    val cookieJar: CookieJar by lazy { PlatformCookieJar() }

    private val clientBuilder = OkHttpClient.Builder().apply {
        cookieJar(cookieJar)
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        callTimeout(2, TimeUnit.MINUTES)

        cache(Cache(
            directory = FileKit.cacheDir.resolve("network_cache").toJavaFile(),
            maxSize = 5L * 1024 * 1024, // 5 MiB
        ))
        
        addInterceptor(UncaughtExceptionInterceptor())
        addInterceptor(UserAgentInterceptor(::defaultUserAgentProvider))
        addNetworkInterceptor(IgnoreGzipInterceptor())
        addNetworkInterceptor(BrotliInterceptor)
    }

    @PlatformSdk
    val client = clientBuilder.apply {
        addInterceptor(CloudflareInterceptor(
            cookieJar as PlatformCookieJar,
            ::defaultUserAgentProvider)
        )
    }.build()

    /**
     * @deprecated Since extension-lib 1.5
     */
    @Deprecated("The regular client handles Cloudflare by default")
    @PlatformSdk
    val cloudflareClient: OkHttpClient = client

    @PlatformSdk
    fun defaultUserAgentProvider() = Awery.defaultUserAgent
}

expect class PlatformCookieJar(): CookieJar