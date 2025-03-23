package com.mrboomdev.awery.ui.utils

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

fun viewModelFactory(factory: (SavedStateHandle) -> ViewModel): ViewModelProvider.Factory {
	return object : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
			@Suppress("UNCHECKED_CAST")
			return factory(extras.createSavedStateHandle()) as T
		}
	}
}