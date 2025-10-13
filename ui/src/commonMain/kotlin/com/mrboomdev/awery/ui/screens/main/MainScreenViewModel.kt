package com.mrboomdev.awery.ui.screens.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.toMedia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// TODO: Delete this file once tv will use separate screen
@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModel(savedStateHandle: SavedStateHandle): ViewModel() {
    val continueWatching = Awery.database.progress
        .observeLatest(25)
        .map { all ->
            all.map { watchProgress ->
				val media = Awery.database.media.get(
                    extensionId = watchProgress.extensionId, 
                    id = watchProgress.mediaId
                ) ?: return@map null

				watchProgress to media.toMedia()
            }.filterNotNull()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}