package com.mrboomdev.awery.core

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.webkit.WebView
import android.os.Build
import android.content.res.Configuration
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.getSystemService
import java.io.File
import java.lang.ref.WeakReference
import androidx.core.net.toUri
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.toJavaFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.resolve
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.http.HttpHeaders.ContentEncoding

private val handler by lazy {
    Handler(Looper.getMainLooper())
}

/**
 * Shows a toast message.
 *
 * @param message The message to show.
 * @param length The duration to show the toast for.
 * @see Toast.LENGTH_SHORT
 * @see Toast.LENGTH_LONG
 */
fun Awery.toast(
    message: String,
    length: Int = Toast.LENGTH_SHORT
) {
    handler.post { Toast.makeText(context, message, length).show() }
}

/**
 * Global application context.
 */
var Awery.context: Application
    get() = androidApplication!!
    set(value) { androidApplication = value }

/**
 * The current activity, if any.
 * This is stored as a [WeakReference] to avoid memory leaks.
 */
var Awery.activity: Activity?
    get() = androidActivity?.get()
    set(value) { androidActivity = WeakReference(value) }

private var androidApplication: Application? = null
private var androidActivity: WeakReference<Activity>? = null

@Suppress("DEPRECATION")
private val isTvImpl by lazy {
    with(androidApplication!!) {
        packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                || packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
                || getSystemService(UiModeManager::class.java).currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
                || !packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }
}

@SuppressLint("WebViewApiAvailability")
private fun getChromeVersion(): String {
    val webViewPackage = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WebView.getCurrentWebViewPackage()
    } else null

    if(webViewPackage == null) {
        return fallbackChromeVersion
    }

    return try {
        androidApplication!!.packageManager.getPackageInfo(
            webViewPackage.packageName, 0
        ).versionName
    } catch(_: PackageManager.NameNotFoundException) { null } ?: fallbackChromeVersion
}

actual object Awery {
    actual fun copyToClipboard(text: String) {
        context.getSystemService<ClipboardManager>()!!
            .setPrimaryClip(ClipData.newPlainText(text, text))
    }
    
    actual fun openUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder().apply { 
            setShowTitle(true)
            setBackgroundInteractionEnabled(true)
            setUrlBarHidingEnabled(true)
        }.build().apply {
            intent.data = url.toUri()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        customTabsIntent.intent.resolveActivity(
            context.packageManager
        )?.also {
            context.startActivity(
                customTabsIntent.intent, 
                customTabsIntent.startAnimationBundle
            )
        } ?: run {
            Log.e("Awery", "No browser app was found!")
            toast("Cannot open url. Do you have a browser?", 1)
        }
    }

    actual val isTv: Boolean
        get() = isTvImpl

    actual val defaultUserAgent: String
        get() {
            val androidVersion = Build.VERSION.RELEASE
            val deviceModel = Build.MODEL
            val chromeVersion = getChromeVersion()

            return "Mozilla/5.0 (Linux; Android $androidVersion; $deviceModel) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/$chromeVersion Mobile Safari/537.36"
        }

    actual val platform: Platform
        get() = Platform.ANDROID

    actual fun share(text: String) {
        IntentBuilder(context)
            .setType("text/plain")
            .setText(text)
            .createChooserIntent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
    }
}

internal actual fun HttpClientConfig<*>.setup() {
    install(HttpCache) {
        publicStorage(FileStorage(FileKit.cacheDir.resolve("http/public").toJavaFile()))
        privateStorage(FileStorage(FileKit.cacheDir.resolve("http/private").toJavaFile()))
    }
}