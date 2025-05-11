package com.mrboomdev.awery.util.extensions

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.mrboomdev.awery.app.AweryLifecycle.Companion.postRunnable

private const val TAG = "ActivityExtensions"

/**
 * There is a bug in an appcompat library which sometimes throws an [NullPointerException].
 * This method tries to do it without throwing any exceptions.
 */
fun Activity.setContentViewCompat(view: View) {
    try {
        setContentView(view)
    } catch (e: NullPointerException) {
        Log.e(TAG, "Failed to setContentView!", e)

        // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method
        //     'void androidx.appcompat.widget.ContentFrameLayout.setDecorPadding(int, int, int, int)' on a null object reference

        // at androidx.appcompat.app.AppCompatDelegateImpl.applyFixedSizeWindow(AppCompatDelegateImpl)
        postRunnable { setContentViewCompat(view) }
    }
}

// The light scrim color used in the platform API 29+
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/com/android/internal/policy/DecorView.java;drc=6ef0f022c333385dba2c294e35b8de544455bf19;l=142
private val DefaultLightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

// The dark scrim color used in the platform.
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/res/color/system_bar_background_semi_transparent.xml
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/remote_color_resources_res/values/colors.xml;l=67
private val DefaultDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

/**
 * Safely enables the "Edge to edge" experience.
 * I really don't know why, but sometimes it just randomly crashes!
 * Because of it we have to rerun this method on a next frame.
 * @author MrBoomDev
 */
@JvmOverloads
fun ComponentActivity.enableEdgeToEdge(
    statusBarStyle: SystemBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
    navigationBarStyle: SystemBarStyle = SystemBarStyle.auto(DefaultLightScrim, DefaultDarkScrim)
) {
    try {
        enableEdgeToEdge(statusBarStyle, navigationBarStyle)
    } catch(e: RuntimeException) {
        Log.e(TAG, "Failed to enable EdgeToEdge! Will retry a little bit later.", e)
        postRunnable { enableEdgeToEdge() }
    }
}