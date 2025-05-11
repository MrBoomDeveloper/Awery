package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
actual fun BackEffect(
	isEnabled: Boolean, 
	onBack: suspend (progress: Flow<Float>) -> Unit
) {
	// TODO: Handle some back key event and then invoke onBack with 1
}