package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

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