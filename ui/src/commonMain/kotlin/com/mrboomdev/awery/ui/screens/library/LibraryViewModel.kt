package com.mrboomdev.awery.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.toMedia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class LibraryViewModel: ViewModel() {
	val isNoLists = Awery.database.lists.observeCount()
		.map { it == 0 }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = false
		)

	@OptIn(ExperimentalCoroutinesApi::class)
	val lists = Awery.database.lists.observeAll()
		.flatMapLatest { lists ->
			combine(lists.map { list ->
				Awery.database.lists.observeMediaInList(list.id)
			}) { lists.zip(it) { list, mediaList ->
				list to mediaList.map { media ->
					media to media.toMedia()
				}
			} }
		}
		.map { true to it }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = false to emptyList()
		)
}