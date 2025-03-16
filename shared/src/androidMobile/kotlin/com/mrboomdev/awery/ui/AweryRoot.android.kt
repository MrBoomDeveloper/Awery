package com.mrboomdev.awery.ui

import androidx.compose.runtime.Composable

@Composable
actual fun AweryRootImpl(content: @Composable () -> Unit) {
	AweryRootAndroid(content)
}