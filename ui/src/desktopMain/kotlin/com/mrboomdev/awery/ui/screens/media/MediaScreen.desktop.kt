package com.mrboomdev.awery.ui.screens.media

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.ui.Routes

@Composable
actual fun MediaScreen(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel
) {
    DefaultMediaScreen(destination, viewModel)
}