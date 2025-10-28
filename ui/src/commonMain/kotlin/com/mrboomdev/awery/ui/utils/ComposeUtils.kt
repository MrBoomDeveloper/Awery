package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

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

@Stable
fun Alignment.Horizontal.toTextAlign() = when(this) {
	Alignment.Start -> TextAlign.Start
	Alignment.End -> TextAlign.End
	Alignment.CenterHorizontally -> TextAlign.Center
	else -> throw UnsupportedOperationException("Cannot convert ${this::class.qualifiedName} to TextAlign!")
}

@Composable
fun Int.toDp(): Dp {
	return with(LocalDensity.current) {
		this@toDp.toDp()
	}
}