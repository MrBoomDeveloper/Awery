package com.mrboomdev.awery.ui.utils

import android.annotation.SuppressLint
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@SuppressLint("NoCollectCallFound")
@Composable
actual fun BackEffect(
	isEnabled: Boolean, 
	onBack: suspend (progress: Flow<Float>) -> Unit
) {
	PredictiveBackHandler(isEnabled) { flow ->
		onBack(flow.map { it.progress })
	}
}