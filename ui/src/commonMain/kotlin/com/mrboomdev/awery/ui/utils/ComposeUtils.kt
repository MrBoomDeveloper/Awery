package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Suppress("FunctionName")
fun KeyboardAction(onAction: () -> Unit) = KeyboardActions(
	onDone = { onAction() },
	onGo = { onAction() },
	onSearch = { onAction() },
	onSend = { onAction() }
)

@Composable
fun RememberLaunchedEffect(
	key1: Any?,
	block: suspend () -> Unit
) {
	var didExecute by rememberSaveable(key1) { mutableStateOf(false) }
	
	LaunchedEffect(didExecute) {
		if(didExecute) return@LaunchedEffect
		didExecute = true
		block()
	}
}

@Composable
@Stable
fun Int.toDp(): Dp {
	return with(LocalDensity.current) {
		this@toDp.toDp()
	}
}