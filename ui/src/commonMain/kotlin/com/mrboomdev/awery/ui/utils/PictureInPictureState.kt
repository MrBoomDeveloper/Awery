package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPictureInPictureState(
    autoEnter: Boolean = false,
    aspectRatio: Pair<Int, Int>? = null
): PictureInPictureState

expect class PictureInPictureState {
    fun enter()
    val isActive: Boolean
    val isSupported: Boolean
}