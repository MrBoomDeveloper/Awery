package com.mrboomdev.awery.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
	private var job: Job? = null
	
	private val _items = mutableStateListOf<Media>()
	val items: List<Media> = _items
	
	private val _error = mutableStateOf<Throwable?>(null)
	val error by _error
	
	private val _extension = mutableStateOf<SettingsPages.Extension?>(null)
	val extension by _extension
	
	val feeds = Extensions.getAll<CatalogModule>(enabled = true).map { 
		
	}
	
	fun reload() {
		job?.cancel()
		
		job = viewModelScope.launch(Dispatchers.Default) { 
			load(page = 0)
		}
	}
	
	private suspend fun load(page: Int) {
		if(page == 0) {
			
		}
	}
}