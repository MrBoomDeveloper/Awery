package com.mrboomdev.awery.core

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

/**
 * Global application context.
 */
var Awery.context: Context
    get() = androidContext!!
    set(value) { androidContext = value }

@SuppressLint("StaticFieldLeak")
private var androidContext: Context? = null

@Suppress("DEPRECATION")
private val isTv by lazy {
    with(androidContext!!) {
        packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                || packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
                || getSystemService(UiModeManager::class.java).currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
                || !packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }
}

actual fun isTv() = isTv