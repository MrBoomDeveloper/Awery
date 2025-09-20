package com.mrboomdev.awery.ui.effects

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackEffect(onBack: () -> Unit) {
	BackHandler(true, onBack)
}