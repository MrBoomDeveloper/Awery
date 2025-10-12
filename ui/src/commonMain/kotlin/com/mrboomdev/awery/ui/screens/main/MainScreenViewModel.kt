package com.mrboomdev.awery.ui.screens.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.CacheStorage
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.toMedia
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Results
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

private val feedsCache by lazy {
    runBlocking {
        CacheStorage<Results<Media>>(
            directory = FileKit.cacheDir / "feeds",
            maxSize = 5 * 1024 * 1024 * 8, // 5 mb
            maxAge = 24 * 60 * 60 * 1000 // 1 day
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModel(savedStateHandle: SavedStateHandle): ViewModel() {
    val isNoLists = Awery.database.lists.observeCount()
        .map { it == 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    
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