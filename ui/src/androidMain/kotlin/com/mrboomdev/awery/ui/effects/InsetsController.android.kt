package com.mrboomdev.awery.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mrboomdev.awery.core.utils.toActivity

private val insets = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()

private fun WindowInsetsControllerCompat.showInsets() {
    show(insets)
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
}

private fun WindowInsetsControllerCompat.hideInsets() {
    hide(insets)
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

@Composable
actual fun InsetsController(hideBars: Boolean) {
    val context = LocalContext.current

    DisposableEffect(hideBars) {
        val window = context.toActivity()?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        insetsController.apply {
            if(hideBars) hideInsets() else showInsets()
        }

        onDispose {
            insetsController.showInsets()
        }
    }
}