package com.mrboomdev.awery.ui.screens.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.core.utils.LoadingStatus
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.data.AgeRating
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.Feed
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Results
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.ic_search
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.FeedRow
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.awery.ui.utils.pagination.InfiniteScroll
import com.mrboomdev.navigation.core.safePop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionScreen(
    destination: Routes.Extension,
    viewModel: ExtensionScreenViewModel = viewModel {
		ExtensionScreenViewModel(destination.extensionId) },
	contentPadding: PaddingValues
) {
    val topBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val navigation = Navigation.current()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topBarBehavior.nestedScrollConnection),
        
        contentWindowInsets = WindowInsets.none,
        containerColor = Color.Transparent,

        topBar = {
            TopAppBar(
                scrollBehavior = topBarBehavior,
                windowInsets = contentPadding.exclude(bottom = true).asWindowInsets(),

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.let {
                        if(isAmoledTheme()) it.copy(alpha = .9f) else it
                    }
                ),

                navigationIcon = {
                    IconButton(
                        padding = 4.dp,
                        painter = painterResource(Res.drawable.ic_back),
                        contentDescription = null,
                        onClick = { navigation.safePop() }
                    )
                },
                
                actions = {
                    IconButton(
                        padding = 10.dp,
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = null,
                        onClick = { navigation.push(Routes.ExtensionSearch(
                            destination.extensionId, 
                            destination.extensionName
                        )) }
                    )
                },

                title = {
                    Text(destination.extensionName)
                }
            )
        }
    ) { scaffoldContentPadding ->
        val state = rememberLazyListState()
        
        InfiniteScroll(state, 2) {
			viewModel.loadMoreFeeds()
		}

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = scaffoldContentPadding.only(vertical = true)
                .plus(contentPadding.only(bottom = true))
                .add(bottom = 16.dp),
            verticalArrangement = if(viewModel.feeds.isEmpty()) {
                Arrangement.Center
            } else Arrangement.Top
        ) { 
            if(viewModel.feeds.isEmpty()) {
                singleItem("empty")
            }

            items(
                items = viewModel.feeds,
                key = { "feed_${it.id}" },
                contentType = { "feed" }
            ) { feed -> 
				val result = viewModel.loadedFeeds[feed]
				
				if(result?.isSuccess == true) {
					val results = result.getOrThrow()
					var showActionsDialog by remember { mutableStateOf<Media?>(null) }
                    
                    showActionsDialog?.also { media ->
                        MediaActionsDialog(
                            extensionId = destination.extensionId,
                            media = media,
                            onDismissRequest = { showActionsDialog = null }
                        )
                    }
					
					fun onOpen() { 
						navigation.push(Routes.ExtensionFeed(
							extensionId = destination.extensionId, 
							extensionName = destination.extensionName, 
							feedId = feed.id, 
							feedName = feed.name
						)) 
					}
                    
					FeedRow(
						modifier = Modifier
							.fillMaxWidth()
                            .animateItem()
                            .thenIf(results.hasNextPage) {
                                if(Awery.platform != Platform.DESKTOP) {
                                    clickable { onOpen() }
                                } else this
                            },
                        
                        contentPadding = contentPadding.only(horizontal = true)
                            .add(horizontal = 18.dp, vertical = 8.dp),
                        
                        title = feed.name,
                        
                        items = results.items.filter { mediaItem ->
                            when(AwerySettings.adultContent.value) {
                                AwerySettings.AdultContent.SHOW -> true

                                AwerySettings.AdultContent.ONLY ->
                                    mediaItem.ageRating?.let { AgeRating.of(it) } == AgeRating.NSFW

                                AwerySettings.AdultContent.HIDE ->
                                    mediaItem.ageRating?.let { AgeRating.of(it) } != AgeRating.NSFW
                            }
                        },

                        actions = {
                            if(results.hasNextPage) {
                                IconButton(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .scale(scaleX = -2f, scaleY = 2f),
                                    padding = 0.dp,
                                    painter = painterResource(Res.drawable.ic_back),
                                    contentDescription = null,
                                    onClick = ::onOpen
                                )
                            }
                        },
                        
                        onMediaLongClick = { media ->
                            showActionsDialog = media
                        },
                        
                        onMediaSelected = { media ->
                            navigation.push(
                                Routes.Media(
                                    destination.extensionId,
                                    destination.extensionName,
                                    media
                                )
                            )
                        }
                    )

                    return@items
                }

                FeedRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),

					contentPadding = contentPadding.only(horizontal = true)
						.add(horizontal = 18.dp, vertical = 8.dp),
                    
                    title = feed.name
                ) { contentPadding ->
                    if(result?.isFailure == true) {
						val error = remember(result) {
							result.exceptionOrNull()!!.classify().message
						}
						
                        Text(
							modifier = Modifier
								.clip(RoundedCornerShape(8.dp))
								.combinedClickable(onLongClick = { Awery.copyToClipboard(error) })
								.padding(contentPadding)
								.padding(top = 6.dp),
							maxLines = 5,
							text = error
						)

                        return@FeedRow
                    }

                    Box(
                        modifier = Modifier
                            .padding(contentPadding)
                            .padding(top = 32.dp, bottom = 16.dp)
                    ) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }

            singleItem("loadingStatus") {
                when(val status = viewModel.loadingStatus) {
                    LoadingStatus.Loading, LoadingStatus.NotInitialized -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is LoadingStatus.Failed -> {
                        SelectionContainer {
                            InfoBox(
                                title = "Problem occurred",
                                message = status.throwable.classify().message
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

class ExtensionScreenViewModel(
    private val extensionId: String
): ViewModel() {
    private var extension: Extension? = null

    private val _loadingStatus = mutableStateOf<LoadingStatus>(LoadingStatus.NotInitialized)
    val loadingStatus by _loadingStatus

    val loadedFeeds = mutableStateMapOf<Feed, Result<Results<Media>>>()
    val feeds = mutableStateListOf<Feed>()
    private var currentPage = 0

    init {
        viewModelScope.launch(Dispatchers.Default) {
            extension = Extensions[extensionId]
            loadMoreFeeds()
        }
    }

    fun loadMoreFeeds() {
        extension?.loadException?.also {
            _loadingStatus.value = LoadingStatus.Failed(it)
            return
        }
        
        if(_loadingStatus.value != LoadingStatus.NotInitialized || extension == null) return
        _loadingStatus.value = LoadingStatus.Loading

        viewModelScope.launchTrying(Dispatchers.IO, onCatch = {
            _loadingStatus.value = LoadingStatus.Failed(it)
        }) {
            extension!!.get<CatalogModule>()!!.getFeeds(currentPage).also { results ->
                if(results.items.isEmpty()) {
                    if(results.hasNextPage) {
                        currentPage++
                        loadMoreFeeds()
                    } else {
                        _loadingStatus.value = LoadingStatus.Loaded
                    }

                    return@also
                }

                results.items.forEach { feed ->
                    feeds.add(feed)
                    loadFeed(feed)
                }

                _loadingStatus.value = if(results.hasNextPage) {
                    LoadingStatus.NotInitialized
                } else LoadingStatus.Loaded
            }

            currentPage++
        }
    }

    fun loadFeed(feed: Feed) {
        viewModelScope.launchTrying(Dispatchers.IO, onCatch = {
            loadedFeeds[feed] = Result.failure(it)
            Log.e("ExtensionScreen", "Failed to load an feed!", it)
        }) {
            extension!!.get<CatalogModule>()!!.loadFeed(feed, currentPage).also { results ->
                if(results.items.isEmpty()) {
                    loadedFeeds[feed] = Result.failure(NothingFoundException("0 results fetched from the feed!"))
                    return@also
                }

                loadedFeeds[feed] = Result.success(results)
            }
        }
    }
}