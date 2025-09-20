package com.mrboomdev.awery.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.toMedia
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.empty_library_title
import com.mrboomdev.awery.resources.ic_collections_bookmark_outlined
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.FeedRow
import com.mrboomdev.awery.ui.components.MediaCard
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.classify
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private enum class LibraryState {
    LOADING, EMPTY, COOL
}

@Composable
fun LibraryPage(
    viewModel: MainScreenViewModel,
    contentPadding: PaddingValues
): () -> Unit {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val isNoLists by viewModel.isNoLists.collectAsState()
    val lists by viewModel.lists.collectAsState()
    val navigation = Navigation.current()

    Crossfade(when {
        lists.second.isNotEmpty() -> LibraryState.COOL
        isNoLists -> LibraryState.EMPTY
        else -> LibraryState.LOADING
    }) { libraryState ->
        when(libraryState) {
            LibraryState.EMPTY -> {
                EmptyLibrary(contentPadding)
            }
            
            LibraryState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            LibraryState.COOL -> {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding.add(bottom = 16.dp)
                ) {
                    items(
                        items = lists.second,
                        key = { it.first.id },
                        contentType = { "list" }
                    ) { (list, mediaList) ->
                        if(mediaList.isNotEmpty()) {
                            FeedRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                title = list.name
                            ) { paddingValues ->
                                Spacer(Modifier.height(16.dp))

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = paddingValues,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = mediaList,
                                        key = { it.first.id }
                                    ) { (dbMedia, media) ->
                                        var showActionsDialog by remember { mutableStateOf(false) }

                                        if(showActionsDialog) {
                                            MediaActionsDialog(
                                                extensionId = dbMedia.extensionId,
                                                media = media,
                                                onDismissRequest = { showActionsDialog = false }
                                            )
                                        }

                                        MediaCard(
                                            media = media,

                                            onClick = {
                                                val extension = runBlocking { Extensions[dbMedia.extensionId] }

                                                navigation.push(Routes.Media(
                                                    extensionId = dbMedia.extensionId,
                                                    extensionName = extension?.name,
                                                    media = media
                                                ))
                                            },

                                            onLongClick = { showActionsDialog = true }
                                        )
                                    }
                                }
                            }
                        } else {
                            FeedRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                title = list.name
                            ) { contentPadding ->
                                Text(
                                    modifier = Modifier
                                        .padding(contentPadding)
                                        .padding(top = 6.dp),
                                    text = "This list is empty.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    return {
        coroutineScope.launch { 
            lazyListState.animateScrollToItem(0)
        }
    }
}

@Composable
private fun EmptyLibrary(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Spacer(Modifier.weight(.75f))

        Icon(
            modifier = Modifier
                .size(96.dp)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            painter = painterResource(Res.drawable.ic_collections_bookmark_outlined),
            contentDescription = null
        )

        Text(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            text = stringResource(Res.string.empty_library_title)
        )

        Spacer(Modifier.weight(1f))
    }
}