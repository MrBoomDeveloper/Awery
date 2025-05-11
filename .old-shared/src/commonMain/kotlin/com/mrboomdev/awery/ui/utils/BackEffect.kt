package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

/**
 * Will be invoked once an Back Navigation has started.
 */
@Composable
expect fun BackEffect(
	isEnabled: Boolean,
	onBack: suspend (progress: Flow<Float>) -> Unit
)