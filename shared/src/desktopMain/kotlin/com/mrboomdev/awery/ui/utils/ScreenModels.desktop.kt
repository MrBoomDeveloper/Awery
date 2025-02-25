package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleStore
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.screen.Screen
import kotlin.reflect.KClass

// On desktop system don't kill your app, so we may just use a regular rememberScreenModel
@Composable
actual fun <T: ScreenModel> Screen.screenModel(
	clazz: KClass<T>,
	tag: String?,
	factory: (SavedStateHandle) -> T
): T {
	val screenModelStore = remember(this) {
		ScreenLifecycleStore.get(this) { ScreenModelStore }
	}
	
	val rememberKey = "$key:${clazz.qualifiedName}:${tag ?: "default"}"
	
	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	return remember(rememberKey) {
		screenModelStore.lastScreenModelKey.value = rememberKey
		@Suppress("UNCHECKED_CAST")
		screenModelStore.screenModels.getOrPut(rememberKey) { factory(SavedStateHandle()) } as T
	}
}