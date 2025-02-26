package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlin.reflect.KClass

/**
 * This function allows us to use an [SavedStateHandle] to restore state after an process death :)
 */
@Composable
inline fun <reified T: ScreenModel> Screen.screenModel(
	tag: String? = null,
	noinline factory: (SavedStateHandle) -> T
) = screenModel(T::class, tag, factory)

@Composable
expect fun <T: ScreenModel> Screen.screenModel(
	clazz: KClass<T>,
	tag: String? = null,
	factory: (SavedStateHandle) -> T
): T