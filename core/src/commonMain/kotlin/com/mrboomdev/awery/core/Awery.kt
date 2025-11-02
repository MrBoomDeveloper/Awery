package com.mrboomdev.awery.core

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*

/**
 * Main object, that stores all the global variables and functions.
 * Also, this object is a bridge to the platform-specific code.
 */
expect object Awery {
    /**
     * Copies the given text to the clipboard.
     *
     * @param text The text to copy.
     */
    fun copyToClipboard(text: String)
    
    /**
     * Opens the given URL in the default browser of the device.
     *
     * @param url The URL to open.
     */
    fun openUrl(url: String)

    /**
     * Shares the given text to the default sharing app of the device.
     *
     * @param text The text to share.
     */
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