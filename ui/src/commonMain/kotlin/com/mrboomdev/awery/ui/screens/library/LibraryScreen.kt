package com.mrboomdev.awery.ui.screens.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.screens.main.LibraryPage
import com.mrboomdev.awery.ui.screens.main.MainScreenViewModel
import com.mrboomdev.awery.ui.utils.viewModel

@Composable
fun LibraryScreen(
	viewModel: LibraryViewModel = viewModel { LibraryViewModel() },
	contentPadding: PaddingValues
) {
	LibraryPage(
		viewModel = viewModel(::MainScreenViewModel),
		contentPadding = contentPadding
	)
}