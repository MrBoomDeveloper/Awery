package com.mrboomdev.awery.ui.utils

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import com.mrboomdev.awery.core.utils.toActivity

actual class PictureInPictureState(
    private val activity: Activity?,
    private val autoEnter: Boolean,
    private val aspectRatio: Pair<Int, Int>?
) {
    private val _isActive = mutableStateOf(activity?.isInPictureInPictureMode ?: false)

    private val pipListener = Consumer<PictureInPictureModeChangedInfo> { info ->
        _isActive.value = info.isInPictureInPictureMode
    }

    fun init() {
        if(activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        if(activity is ComponentActivity) {
            activity.addOnPictureInPictureModeChangedListener(pipListener)
        }

        activity.setPictureInPictureParams(PictureInPictureParams.Builder().apply {
            if(aspectRatio != null) {
                setAspectRatio(Rational(aspectRatio.first, aspectRatio.second))
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setAutoEnterEnabled(autoEnter)
            }
        }.build())
    }

    fun dispose() {
        if(activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        activity.setPictureInPictureParams(PictureInPictureParams.Builder().build())

        if(activity is ComponentActivity) {
            activity.removeOnPictureInPictureModeChangedListener(pipListener)
        }
    }

    actual fun enter() {
        @Suppress("DEPRECATION")
        activity?.enterPictureInPictureMode()
    }

    actual val isSupported: Boolean
        get() = activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true

    actual val isActive by _isActive
}

@Composable
actual fun rememberPictureInPictureState(
    autoEnter: Boolean,
    aspectRatio: Pair<Int, Int>?
): PictureInPictureState {
    val activity = LocalContext.current.toActivity()

    val state = remember(activity, autoEnter, aspectRatio) {
        PictureInPictureState(activity, autoEnter, aspectRatio)
    }

    DisposableEffect(state) {
        state.init()
        onDispose { state.dispose() }
    }

    return state
}

@Composable
fun isInPictureInPicture(): Boolean {
    val activity = LocalContext.current.toActivity()
    if(activity == null) return false

    var state by remember { mutableStateOf(activity.isInPictureInPictureMode) }

    DisposableEffect(Unit) {
        val listener = Consumer<PictureInPictureModeChangedInfo> { info ->
            state = info.isInPictureInPictureMode
        }

        if(activity is ComponentActivity) {
            activity.addOnPictureInPictureModeChangedListener(listener)
            onDispose { activity.removeOnPictureInPictureModeChangedListener(listener) }
        } else onDispose {}
    }

    return state
}