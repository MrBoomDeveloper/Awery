package com.mrboomdev.awery.ui.screens.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mrboomdev.awery.ui.App
import com.mrboomdev.awery.ui.effects.PostLaunchedEffect
import com.mrboomdev.awery.ui.screens.main.MainScreenViewModel
import com.mrboomdev.awery.ui.screens.main.SearchPage
import com.mrboomdev.awery.ui.utils.viewModel

@Composable
fun SearchScreen(
	viewModel: SearchViewModel = viewModel { SearchViewModel() },
	contentPadding: PaddingValues
) {
	val query by App.searchQuery.collectAsState()

	PostLaunchedEffect(query) {
		
	}
	
	SearchPage(
		viewModel = viewModel(::MainScreenViewModel),
		contentPadding = contentPadding,
		query = App.searchQuery.collectAsState().value
	)
}