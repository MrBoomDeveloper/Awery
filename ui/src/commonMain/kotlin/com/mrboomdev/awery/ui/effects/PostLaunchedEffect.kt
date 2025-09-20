package com.mrboomdev.awery.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope

/**
 * Launches [block] only once [key] has changed. Initial LaunchedEffect is being ignored!
 * Key is being saved so that you could observe changes after popping back from a different screen.
 */
@Composable
fun PostLaunchedEffect(key: Any?, block: suspend CoroutineScope.() -> Unit) {
	var prevKey by rememberSaveable { mutableStateOf(key) }
	
	LaunchedEffect(key) {
		if(key == prevKey) {
			return@LaunchedEffect
		}
		
		block()
		prevKey = key
	}
}