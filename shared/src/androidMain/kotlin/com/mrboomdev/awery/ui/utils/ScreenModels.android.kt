package com.mrboomdev.awery.ui.utils

import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedStateRegistryOwner
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleStore
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.screen.Screen
import kotlin.reflect.KClass

@Composable
actual fun <T: ScreenModel> Screen.screenModel(
	clazz: KClass<T>,
	tag: String?,
	factory: (SavedStateHandle) -> T
): T {
	var context = LocalContext.current
		
	while(context !is SavedStateRegistryOwner) {
		if(context is ContextWrapper) {
			context = context.baseContext
		} else {
			throw UnsupportedOperationException("Activity does not extend SavedStateRegistryOwner!")
		}
	}
	
	val savedStateRegistryOwner: SavedStateRegistryOwner = context
	val registry = savedStateRegistryOwner.savedStateRegistry
	
	val savedStateKey = "screenModel;;;$key;;;savedState"
	val rememberKey = "$key:${clazz.qualifiedName}:${tag ?: "default"}"
	
	@SuppressWarnings("RestrictedApi")
	val savedStateHandle = SavedStateHandle.createHandle(
		registry.consumeRestoredStateForKey(savedStateKey), null
	).apply {
		DisposableEffect(rememberKey) {
			registry.registerSavedStateProvider(savedStateKey, savedStateProvider())
			
			onDispose { 
				registry.unregisterSavedStateProvider(savedStateKey)
			}
		}
	}
	
	val screenModelStore = remember(this) {
		ScreenLifecycleStore.get(this) { ScreenModelStore }
	}

	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
	return remember(rememberKey) {
		screenModelStore.lastScreenModelKey.value = rememberKey
		@Suppress("UNCHECKED_CAST")
		screenModelStore.screenModels.getOrPut(rememberKey) { 
			factory(savedStateHandle) 
		} as T
	}
}