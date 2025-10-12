package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.text.KeyboardActions

@Suppress("FunctionName")
fun KeyboardAction(onAction: () -> Unit) = KeyboardActions(
	onDone = { onAction() },
	onGo = { onAction() },
	onSearch = { onAction() },
	onSend = { onAction() }
)