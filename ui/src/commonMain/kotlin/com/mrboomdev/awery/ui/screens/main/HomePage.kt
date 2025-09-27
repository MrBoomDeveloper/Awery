package com.mrboomdev.awery.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.ic_language
import com.mrboomdev.awery.resources.ic_refresh
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.ExpandableText
import com.mrboomdev.awery.ui.components.FeedRow
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.effects.PostLaunchedEffect
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.awery.ui.utils.pagination.InfiniteScroll
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomePage(
    viewModel: MainScreenViewModel,
    contentPadding: PaddingValues
): () -> Unit {
    val coroutineScope = rememberCoroutineScope()
    val navigation = Navigation.current()
    val windowSize = currentWindowSize()
    val lazyListState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val loadedFeeds by viewModel.loadedFeeds.collectAsState()
    val failedFeeds by viewModel.failedFeeds.collectAsState()
    val isLoading by viewModel.isLoadingFeeds.collectAsState()
    val isReloading by viewModel.isReloadingFeeds.collectAsState()
    
    InfiniteScroll(lazyListState, 5) {
		viewModel.loadMoreFeeds()
	}

    PostLaunchedEffect(AwerySettings.adultContent.state.value) {
        viewModel.reloadFeeds()
    }
    
    @Composable
    fun Content() {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding
                .exclude(end = true)
                .add(bottom = 16.dp),
            state = lazyListState
        ) {
            if(loadedFeeds.isEmpty()) {
                singleItem("welcome") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 500.dp)
                                .padding(
                                    horizontal = 32.dp,
                                    vertical = when(windowSize.height) {
                                        WindowSizeType.Small -> 32.dp
                                        WindowSizeType.Medium -> 64.dp
                                        WindowSizeType.Large -> 128.dp
                                        WindowSizeType.ExtraLarge -> 256.dp
                                    }
                                ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.displaySmall,
                                text = "Welcome back, ${AwerySettings.username.state.value}!"
                            )

                            Text(
                                textAlign = TextAlign.Center,
                                text = "Sorry, but this app currently is going through a full reworking. " +
                                        "Some stuff that was working previously may be missing in this version. " +
                                        "Thanks for understanding."
                            )
                        }
                    }
                }
            }

            // TODO: Make this section visible once we'll be able to restore a video from a DBWatchProgress
            //            if(continueWatching.isNotEmpty().let { false }) {
            //                singleItem("continueWatching") {
            //                    FeedRow(
            //                        modifier = Modifier
            //                            .fillMaxWidth()
            //                            .animateItem(),
            //                        title = "Continue watching"
            //                    ) { contentPadding ->
            //                        Spacer(Modifier.height(16.dp))
            //
            //                        LazyRow(
            //                            modifier = Modifier.fillMaxWidth(),
            //                            contentPadding = contentPadding,
            //                            horizontalArrangement = Arrangement.spacedBy(8.dp)
            //                        ) {
            //                            items(
            //                                items = continueWatching,
            //                                key = { it.first.extensionId + it.first.mediaId + it.first.variantId }
            //                            ) { (watchProgress, media) ->
            //                                val coroutineScope = rememberCoroutineScope()
            //                                var isLoading by remember { mutableStateOf(false) }
            //                                var job by remember { mutableStateOf<Job?>(null) }
            //
            //                                if(isLoading) {
            //                                    Dialog(onDismissRequest = {
            //                                        job?.cancel()
            //                                        isLoading = false
            //                                    }) {
            //                                        CircularProgressIndicator()
            //                                    }
            //                                }
            //
            //                                Column(
            //                                    modifier = Modifier
            //                                        .clip(RoundedCornerShape(2.dp))
            //                                        .clickable {
            //                                            isLoading = true
            //
            //                                            job = coroutineScope.launchTrying(
            //                                                Dispatchers.Default, onCatch = { t ->
            //                                                    if(t is CancellationException) return@launchTrying
            //                                                    toaster.toast("Failed to play a video")
            //                                                    isLoading = false
            //                                                }
            //                                            ) {
            //                                                val extension = Extensions[watchProgress.extensionId]
            //                                                    .runIfNull {
            //                                                        toaster.toast("Source extension isn't installed!")
            //                                                        return@launchTrying
            //                                                    }
            //
            //
            //
            //                                                toaster.toast("You shouldn't be able to get here!")
            //                                                isLoading = false
            //                                            }
            //                                        },
            //                                    verticalArrangement = Arrangement.spacedBy(12.dp)
            //                                ) {
            //                                    Box(
            //                                        modifier = Modifier
            //                                            .clip(RoundedCornerShape(2.dp))
            //                                            .height(150.dp)
            //                                            .aspectRatio(16f / 9f)
            //                                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            //                                    ) {
            //                                        AsyncImage(
            //                                            modifier = Modifier.fillMaxSize(),
            //                                            model = watchProgress.thumbnail ?: media.banner ?: media.poster,
            //                                            error = painterResource(Res.drawable.poster_no_image),
            //                                            contentScale = ContentScale.Crop,
            //                                            contentDescription = null
            //                                        )
            //
            //                                        if(watchProgress.maxProgress != null) {
            //                                            LinearProgressIndicator(
            //                                                modifier = Modifier
            //                                                    .fillMaxWidth()
            //                                                    .align(Alignment.BottomCenter),
            //                                                progress = {
            //                                                    (watchProgress.progress / watchProgress.maxProgress!!).toFloat()
            //                                                }
            //                                            )
            //                                        }
            //                                    }
            //
            //                                    Text(
            //                                        style = MaterialTheme.typography.bodyMedium,
            //                                        overflow = TextOverflow.Ellipsis,
            //                                        minLines = 2,
            //                                        maxLines = 2,
            //                                        text = media.title
            //                                    )
            //                                }
            //                            }
            //                        }
            //                    }
            //                }
            //            }

            items(
                items = loadedFeeds,
                key = { "feed_${it.first.id}_${it.second.id}" },
                contentType = { "feed" }
            ) { (extension, feed, media) ->
                var showActionsDialog by remember { mutableStateOf<Media?>(null) }

                showActionsDialog?.also { media ->
                    MediaActionsDialog(
                        extensionId = extension.id,
                        media = media,
                        onDismissRequest = { showActionsDialog = null }
                    )
                }

                FeedRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .thenIf(media.hasNextPage && Awery.platform != Platform.DESKTOP) { clickable {
                            navigation.push(Routes.ExtensionFeed(
                                extensionId = extension.id,
                                extensionName = extension.name,
                                feedId = feed.id,
                                feedName = feed.name
                            ))
                        } }
                        .animateItem(),

                    contentPadding = WindowInsets.safeDrawing
                        .only(WindowInsetsSides.End)
                        .asPaddingValues()
                        .add(horizontal = 16.dp, vertical = 8.dp),

                    title = "${extension.name} - ${feed.name}",
                    items = media.items,

                    actions = {
                        if(media.hasNextPage) {
                            IconButton(
                                modifier = Modifier
                                    .size(16.dp)
                                    .scale(scaleX = -2f, scaleY = 2f),
                                padding = 0.dp,
                                painter = painterResource(Res.drawable.ic_back),
                                contentDescription = null,
                                onClick = {
                                    navigation.push(Routes.ExtensionFeed(
                                        extensionId = extension.id,
                                        extensionName = extension.name,
                                        feedId = feed.id,
                                        feedName = feed.name
                                    ))
                                }
                            )
                        }
                    },

                    onMediaLongClick = { media ->
                        showActionsDialog = media
                    },

                    onMediaSelected = {
                        navigation.push(Routes.Media(
                            extensionId = extension.id,
                            extensionName = extension.name,
                            media = it
                        ))
                    }
                )
            }

            if(isLoading) {
                singleItem("loadingIndicator") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .animateItem(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            items(
                items = failedFeeds,
                key = { "failedFeed_${it.first.id}_${it.second.id}" },
                contentType = { "failedFeed" }
            ) { (extension, feed, throwable) ->
                var isReloading by remember { mutableStateOf(false) }

                FeedRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    title = "${extension.name} - ${feed.name}",
                    actions = {
                        if(isReloading) return@FeedRow

                        extension.webpage?.also { webpage ->
                            IconButton(
                                modifier = Modifier.size(42.dp),
                                padding = 9.dp,
                                painter = painterResource(Res.drawable.ic_language),
                                contentDescription = null,
                                onClick = { navigation.push(Routes.Browser(webpage)) }
                            )
                        }

                        IconButton(
                            modifier = Modifier.size(42.dp),
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = null,
                            onClick = {
                                isReloading = true

                                viewModel.reloadFeed(extension, feed) { feedIndex ->
                                    isReloading = false

                                    if(feedIndex != null) {
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(feedIndex)
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) { contentPadding ->
                    Crossfade(isReloading) { isReloading ->
                        if(isReloading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(contentPadding)
                                    .padding(top = 32.dp, bottom = 16.dp)
                            )

                            return@Crossfade
                        }

                        SelectionContainer {
                            var expand by remember { mutableStateOf(false) }

                            ExpandableText(
                                modifier = Modifier
                                    .padding(contentPadding)
                                    .padding(top = 6.dp),
                                isExpanded = expand,
                                onExpand = { expand = it },
                                maxLines = 5,
                                text = throwable.classify().message
                            )
                        }
                    }
                }
            }
        }
    }
    
    if(Awery.platform == Platform.DESKTOP) {
        // PullToRefreshBox gestures doesn't work correctly with mouse
        Content()
    } else {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            isRefreshing = isReloading,
            onRefresh = { viewModel.reloadFeeds() },
            content = { Content() },
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isReloading,
                    state = pullToRefreshState,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
    
    return {
        coroutineScope.launch { 
            lazyListState.animateScrollToItem(0)
        }
    }
}