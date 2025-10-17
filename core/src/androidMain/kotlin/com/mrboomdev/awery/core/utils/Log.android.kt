package com.mrboomdev.awery.core.utils

import android.content.pm.ApplicationInfo
import android.util.Log
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context

private val isDebug by lazy {
    (Awery.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

actual object Log {
    actual fun i(tag: String, message: String) {
        if(!isDebug) return
        Log.i(tag, message)
    }

    actual fun d(tag: String, message: String) {
        if(!isDebug) return
        Log.d(tag, message)
    }

    actual fun w(tag: String, message: String) {
        if(!isDebug) return
        Log.w(tag, message)
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if(!isDebug) return
        Log.e(tag, message, throwable)
    }
}