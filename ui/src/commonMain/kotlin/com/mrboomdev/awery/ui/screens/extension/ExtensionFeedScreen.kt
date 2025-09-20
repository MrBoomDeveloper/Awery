package com.mrboomdev.awery.ui.screens.extension

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.utils.LoadingStatus
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.asyncTryingSupervise
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.core.utils.launchTryingSupervise
import com.mrboomdev.awery.core.utils.mayStartLoading
import com.mrboomdev.awery.data.AgeRating
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Feed
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.MediaCard
import com.mrboomdev.awery.ui.components.Toaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.classify
import com.mrboomdev.awery.ui.utils.pagination.InfiniteScroll
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.viewModel
import com.mrboomdev.navigation.core.Navigation
import com.mrboomdev.navigation.core.safePop
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource

@Suppress("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionFeedScreen(
    destination: Routes.ExtensionFeed,
    viewModel: ExtensionFeedScreenViewModel = run {
        val toaster = LocalToaster.current
        val navigation = Navigation.current()
        
        viewModel {
            ExtensionFeedScreenViewModel(destination, toaster, navigation)
        }
    }
) {
    val topBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyGridState = rememberLazyGridState()
    val navigation = Navigation.current()

	InfiniteScroll(
		state = lazyGridState,
		buffer = 1,
		loadMore = { viewModel.loadMore().await() }
	)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topBarBehavior.nestedScrollConnection),
        
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color.Transparent,
        
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                scrollBehavior = topBarBehavior,

                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                ),

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.let {
                        if(isAmoledTheme()) it.copy(alpha = .9f) else it
                    }
                ),

                navigationIcon = {
                    IconButton(
                        padding = 6.dp,
                        painter = painterResource(Res.drawable.ic_back),
                        contentDescription = null,
                        onClick = { navigation.safePop() }
                    )
                },
                
                title = {
                    Text(destination.extensionName + " - " + destination.feedName)
                }
            )
        }
    ) { contentPadding ->
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = lazyGridState,
            
            contentPadding = contentPadding.add(
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
            
            columns = GridCells.Adaptive(100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = if(viewModel.media.isEmpty()) {
                Arrangement.Center
            } else Arrangement.spacedBy(16.dp, Alignment.Top)
        ) {
            singleItem("scrollFix")
            
            items(
                items = viewModel.media,
                key = { it.id }
            ) { media ->
                var showActionsDialog by remember { mutableStateOf(false) }
                
                if(showActionsDialog) {
                    MediaActionsDialog(
                        extensionId = destination.extensionId,
                        media = media,
                        onDismissRequest = { showActionsDialog = false }
                    )
                }
                
                MediaCard(
                    modifier = Modifier.fillMaxWidth(),
                    media = media,
                    onClick = {
                        navigation.push(Routes.Media(
                            extensionId = destination.extensionId,
                            extensionName = destination.extensionName,
                            media = media
                        ))
                    },
                    onLongClick = { showActionsDialog = true }
                )
            }

            singleItem("loadingStatus") {
                when(val status = viewModel.loadingStatus) {
                    LoadingStatus.Loading, LoadingStatus.NotInitialized -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
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

class ExtensionFeedScreenViewModel(
    private val destination: Routes.ExtensionFeed,
    private val toaster: Toaster,
    private val navigation: Navigation<Routes>
): ViewModel() {
    private val feed = Feed(destination.feedId, destination.feedName)
    private var module: CatalogModule? = null
    private var currentPage = 0
    
    private val _loadingStatus = mutableStateOf<LoadingStatus>(LoadingStatus.NotInitialized)
    val loadingStatus by _loadingStatus
    
    private val _media = mutableStateListOf<Media>()
    val media: List<Media> = _media
    
    fun loadMore(): Deferred<Unit> {
        if(!_loadingStatus.value.mayStartLoading) return viewModelScope.async {}
        _loadingStatus.value = LoadingStatus.Loading
        
        return viewModelScope.asyncTryingSupervise(Dispatchers.Default, onCatch = {
            _loadingStatus.value = LoadingStatus.Failed(it)
            Log.e("ExtensionFeedScreen", "Failed to load an feed!", it)
        }) {
            if(module == null) {
                module = Extensions[destination.extensionId]?.get<CatalogModule>() ?: run { 
                    toaster.toast("Extension is no longer installed!")
                    withContext(Dispatchers.Main) { navigation.safePop() }
                    return@asyncTryingSupervise
                }
            }

            module!!.loadFeed(feed, currentPage).also { results ->
                // Sometimes api may return media items with the same idea due
                // to new content being added during the pagination.
                val newMedia = results.items.filter { item ->
                    media.none { it.id == item.id } && when(AwerySettings.adultContent.value) {
                        AwerySettings.AdultContent.SHOW -> true

                        AwerySettings.AdultContent.ONLY ->
                            item.ageRating?.let { AgeRating.of(it) } == AgeRating.NSFW

                        AwerySettings.AdultContent.HIDE ->
                            item.ageRating?.let { AgeRating.of(it) } != AgeRating.NSFW
                    }
                }

                _media += newMedia

                _loadingStatus.value = if(results.hasNextPage) {
                    LoadingStatus.NotInitialized
                } else LoadingStatus.Loaded

                currentPage++

                if(newMedia.isEmpty()) {
                    loadMore()
                }
            }
        }
    }
}