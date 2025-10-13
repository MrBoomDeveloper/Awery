package com.mrboomdev.awery.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.has
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.ui.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
	private val _extensionsFound = combine(
		App.searchQuery,
		Extensions.observeAll(enabled = true),
		AwerySettings.adultContent.stateFlow
	) { query, extensions, adultContentMode ->
		extensions.filter { extension ->
			extension.name.lowercase().contains(query.lowercase()) && extension.has<CatalogModule>() && when(adultContentMode) {
				AwerySettings.AdultContent.SHOW -> true
				AwerySettings.AdultContent.HIDE -> !extension.isNsfw
				AwerySettings.AdultContent.ONLY -> extension.isNsfw
			}
		}
	}
	
	val extensionsFound = _extensionsFound.map { extensions ->
		extensions.sortedBy { extension ->
			extension.name.lowercase()
		}
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = null
	)
	
	init {
		viewModelScope.launch(Dispatchers.Default) { 
			_extensionsFound.collect { extensions ->
				
			}
		}
	}
}