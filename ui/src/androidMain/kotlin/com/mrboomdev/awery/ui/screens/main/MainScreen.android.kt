package com.mrboomdev.awery.ui.screens.main

import androidx.compose.runtime.Composable
import androidx.tv.material3.Text
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.TV

@Composable
actual fun MainScreen(viewModel: MainScreenViewModel) {
    if(Awery.TV) TvMainScreen(viewModel)
    else DefaultMainScreen(viewModel)
}

@Composable
private fun TvMainScreen(viewModel: MainScreenViewModel) {
    Text("Tv main screen")
}