package com.mrboomdev.awery.ui.effects

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.mrboomdev.awery.core.utils.toActivity

private val ScreenOrientation.androidConstant
    get() = when(this) {
        ScreenOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        ScreenOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    }

@Composable
actual fun RequestScreenOrientation(orientation: ScreenOrientation) {
    val activity = LocalContext.current.toActivity()!!

    DisposableEffect(Unit) {
        val wasOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation.androidConstant
        onDispose { activity.requestedOrientation = wasOrientation }
    }
}