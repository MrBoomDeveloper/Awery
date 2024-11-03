package com.mrboomdev.awery.util.extensions

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.enableEdgeToEdge
import com.mrboomdev.awery.app.AweryLifecycle
import com.mrboomdev.awery.app.AweryLifecycle.Companion.generateRequestCode
import com.mrboomdev.awery.app.AweryLifecycle.Companion.postRunnable
import com.mrboomdev.awery.app.ThemeManager
import java.util.WeakHashMap
import kotlin.reflect.KClass

private const val TAG = "ActivityExtensions"
private val backPressedCallbacks = WeakHashMap<() -> Unit, Any>()

fun Activity.startActivityForResult(
    clazz: KClass<*>? = null,
    action: String? = null,
    type: String? = null,
    categories: Array<String>? = null,
    extras: Map<String, Any>? = null,
    callback: (resultCode: Int, result: Intent?) -> Unit,
    requestCode: Int = generateRequestCode()
) {
    val activity = this

    startActivityForResult(Intent(action).apply {
        setType(type)

        if(categories != null) {
            for(category in categories) {
                addCategory(category)
            }
        }

        if(extras != null) {
            for((key, value) in extras) {
                put(key, value)
            }
        }

        if(clazz != null) {
            component = ComponentName(activity, clazz.java)
        }
    }, callback, requestCode)
}

fun Activity.startActivityForResult(
    intent: Intent,
    callback: (resultCode: Int, result: Intent?) -> Unit,
    requestCode: Int = generateRequestCode()
) {
    runOnUiThread {
        AweryLifecycle.startActivityForResult(this, intent, callback, requestCode)
    }
}

fun Activity.removeOnBackPressedListener(callback: () -> Unit) {
    val onBackInvokedCallback = backPressedCallbacks.remove(callback) ?: return

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        onBackInvokedDispatcher.unregisterOnBackInvokedCallback(callback)
    } else {
        (onBackInvokedCallback as OnBackPressedCallback).remove()
    }
}

fun Activity.addOnBackPressedListener(callback: () -> Unit) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        backPressedCallbacks[callback] = callback
        onBackInvokedDispatcher.registerOnBackInvokedCallback(0, callback)
    } else {
        if(this is OnBackPressedDispatcherOwner) {
            val onBackInvokedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    callback()
                }
            }

            onBackPressedDispatcher.addCallback(this, onBackInvokedCallback)
            backPressedCallbacks[callback] = onBackInvokedCallback
        } else {
            throw IllegalArgumentException("Activity must implement OnBackPressedDispatcherOwner!")
        }
    }
}

fun Activity.applyTheme() {
    ThemeManager.apply(this)
}

/**
 * There is a bug in an appcompat library which sometimes throws an [NullPointerException].
 * This method tries to do it without throwing any exceptions.
 */
fun Activity.setContentViewCompat(view: View) {
    try {
        activity.setContentView(view)
    } catch (e: NullPointerException) {
        Log.e(TAG, "Failed to setContentView!", e)

        // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method
        //     'void androidx.appcompat.widget.ContentFrameLayout.setDecorPadding(int, int, int, int)' on a null object reference

        // at androidx.appcompat.app.AppCompatDelegateImpl.applyFixedSizeWindow(AppCompatDelegateImpl)
        postRunnable { setContentViewCompat(view) }
    }
}

/**
 * Safely enables the "Edge to edge" experience.
 * I really don't know why, but sometimes it just randomly crashes!
 * Because of it we have to rerun this method on a next frame.
 * @author MrBoomDev
 */
fun ComponentActivity.enableEdgeToEdge() {
    try {
        enableEdgeToEdge()
    } catch(e: RuntimeException) {
        Log.e(TAG, "Failed to enable EdgeToEdge! Will retry a little bit later.", e)
        postRunnable { enableEdgeToEdge() }
    }
}