package com.mrboomdev.awery.ui.utils

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun TopAppBarDefaults.transparentTopAppBarColors() = topAppBarColors(
    containerColor = Color.Transparent,
    scrolledContainerColor = Color.Transparent
)

@OptIn(ExperimentalMaterial3Api::class)
suspend fun TopAppBarScrollBehavior.collapse() {
    AnimationState(initialValue = state.heightOffset)
        .animateTo(
            targetValue = state.heightOffsetLimit,
            animationSpec = tween(durationMillis = 500)
        ) { this@collapse.state.heightOffset = value }
}

@OptIn(ExperimentalMaterial3Api::class)
suspend fun TopAppBarScrollBehavior.expand() {
    AnimationState(initialValue = state.heightOffset)
        .animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        ) { this@expand.state.heightOffset = value }
}