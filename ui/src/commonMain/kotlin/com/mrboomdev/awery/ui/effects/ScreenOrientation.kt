package com.mrboomdev.awery.ui.effects

import androidx.compose.runtime.Composable

enum class ScreenOrientation {
    LANDSCAPE, PORTRAIT
}

@Composable
expect fun RequestScreenOrientation(orientation: ScreenOrientation)