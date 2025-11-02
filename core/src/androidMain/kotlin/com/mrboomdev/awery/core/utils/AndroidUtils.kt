package com.mrboomdev.awery.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Attempts to retrieve the [Activity] associated with the given [Context].
 * 
 * This function traverses the context hierarchy until it finds an [Activity] or reaches the root context.
 * If an [Activity] is found, it is returned. Otherwise, null is returned.
 * 
 * @return The associated [Activity], or null if none is found.
 */
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