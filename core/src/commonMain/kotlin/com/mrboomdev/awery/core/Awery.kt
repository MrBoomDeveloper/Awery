package com.mrboomdev.awery.core

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding

/**
 * Main object, that stores all the global variables and functions.
 * Also, this object is a bridge to the platform-specific code.
 */
expect object Awery {
    fun copyToClipboard(text: String)
    fun openUrl(url: String)

    fun share(text: String)

    /**
     * Whatever device is an TV or not.
     */
    val isTv: Boolean

    /**
     * Platform-specific user agent
     */
    val defaultUserAgent: String
    
    val platform: Platform
}

enum class Platform {
    ANDROID,
    DESKTOP
}

internal const val fallbackChromeVersion = "120.0.0"

val Awery.http get() = httpClientImpl
internal expect fun HttpClientConfig<*>.setup()
private val httpClientImpl by lazy {
    HttpClient(CIO) {
        expectSuccess = true
        ContentEncoding()

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        setup()
    }
}