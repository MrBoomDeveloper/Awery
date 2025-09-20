package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable

// No-op implementation because on desktop
// apps are already in the pip mode.
actual object PictureInPictureState {
    actual fun enter() {}
    actual val isActive = false
    actual val isSupported = false
}

@Composable
actual fun rememberPictureInPictureState(
    autoEnter: Boolean,
    aspectRatio: Pair<Int, Int>?
): PictureInPictureState {
    return PictureInPictureState
}