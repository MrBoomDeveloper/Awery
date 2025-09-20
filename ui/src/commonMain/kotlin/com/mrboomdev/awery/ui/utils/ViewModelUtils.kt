package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
inline fun <reified T: ViewModel> viewModel(
    crossinline factory: (SavedStateHandle) -> T
) = viewModel { factory(createSavedStateHandle()) }