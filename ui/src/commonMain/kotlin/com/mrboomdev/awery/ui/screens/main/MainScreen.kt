package com.mrboomdev.awery.ui.screens.main

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) = DefaultMainScreen(viewModel)

@Composable
internal fun DefaultMainScreen(
    viewModel: MainScreenViewModel
) {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }

    Text("Hello, World!")
}