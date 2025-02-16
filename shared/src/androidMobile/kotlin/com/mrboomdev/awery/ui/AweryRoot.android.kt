package com.mrboomdev.awery.ui

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.routes.BaseRoute

@Composable
actual fun AweryRootImpl(content: @Composable () -> Unit) {
	AweryRootAndroid(content)
}