package com.mrboomdev.awery.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.toActivity(): Activity? {
    var current = this

    while(current is ContextWrapper) {
        if(current is Activity) {
            return current
        }

        current = current.baseContext
    }

    return null
}