package com.mrboomdev.awery.ui.screens.main

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.utils.viewModel

// TODO: Delete this file once tv will use separate screen
@Composable
expect fun MainScreen(viewModel: MainScreenViewModel = viewModel(::MainScreenViewModel))

@Composable
internal fun DefaultMainScreen(viewModel: MainScreenViewModel) {}