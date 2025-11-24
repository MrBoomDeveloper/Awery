package com.mrboomdev.awery.ui.screens.extension

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.utils.LoadingStatus
import com.mrboomdev.awery.core.utils.launchTryingSupervise
import com.mrboomdev.awery.data.AgeRating
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.StringPreference
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.navigation.Routes
import com.mrboomdev.awery.ui.components.FlexibleTopAppBar
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.components.MediaCard
import com.mrboomdev.awery.ui.navigation.RouteInfo
import com.mrboomdev.awery.ui.navigation.RouteInfoEffect
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.awery.ui.utils.pagination.InfiniteScroll
import com.mrboomdev.navigation.core.safePop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExtensionSearchScreen(
    destination: Routes.ExtensionSearch,
    viewModel: ExtensionSearchScreenViewModel = viewModel { 
		ExtensionSearchScreenViewModel(destination) 
	},
	contentPadding: PaddingValues
) {
    val contentPadding = contentPadding.add(horizontal = niceSideInset())
    val topBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showFiltersDialog by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val lazyGridState = rememberLazyGridState()
    val navigation = Navigation.current()
    val filters by viewModel.filters.collectAsState()

    RouteInfoEffect(
        displayHeader = false
    )
    
    InfiniteScroll(
		state = lazyGridState,
		buffer = 1,
		loadMore = { viewModel.loadMore() }
	)

    if(showFiltersDialog && filters != null) {
        FiltersDialog(
            filters = filters!!,
            onApplyFilters = { viewModel.applyFilters(it) },
            onDismissRequest = { showFiltersDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topBarBehavior.nestedScrollConnection),

        contentWindowInsets = contentPadding.asWindowInsets(),
        containerColor = Color.Transparent,

        topBar = {
            FlexibleTopAppBar(
                scrollBehavior = topBarBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.let {
                        if(isAmoledTheme()) it.copy(alpha = .9f) else it
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(contentPadding.only(
                            top = true, start = true, end = true))
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        padding = 4.dp,
                        painter = painterResource(Res.drawable.ic_back),
                        contentDescription = null,
                        onClick = { navigation.safePop() }
                    )
                    
                    filters?.firstOrNull { it.role == Preference.Role.QUERY }?.also { queryFilter ->
                        val keyboardController = LocalSoftwareKeyboardController.current
                        
                        Box(Modifier.weight(1f)) {
                            BasicTextField(
                                modifier = Modifier
                                    .widthIn(max = 450.dp)
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    (queryFilter as StringPreference).value = it
                                    viewModel.applyFilters(filters!!)
                                },

                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search
                                ),
                                
                                keyboardActions = KeyboardActions {
                                    keyboardController?.hide()
                                },

                                textStyle = MaterialTheme.typography.bodyLarge
                                    .copy(color = MaterialTheme.colorScheme.onSurface),
                                
                                singleLine = true,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),

                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(32.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.let {
                                                if(isAmoledTheme()) it.copy(alpha = .5f) else it
                                            })
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .padding(start = 16.dp, end = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.weight(1f)) {
                                            searchQuery.also { text ->
                                                innerTextField()

                                                if(text.isEmpty()) {
                                                    Text(
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        text = "Search anything"
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            modifier = Modifier
                                                .thenIf(searchQuery.isBlank()) { alpha(0f) }
                                                .size(32.dp),
                                            padding = 4.dp,
                                            painter = painterResource(Res.drawable.ic_close),
                                            contentDescription = null,
                                            onClick = { searchQuery = "" }
                                        )
                                    }
                                }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AnimatedVisibility(filters?.any { it.role == Preference.Role.QUERY } == true) {
                            IconButton(
                                padding = 10.dp,
                                painter = painterResource(Res.drawable.ic_filter_outlined),
                                contentDescription = null,
                                onClick = { showFiltersDialog = true }
                            )
                        }
                    }
                }
            }
        }
    ) { contentPadding ->
        val media by viewModel.media.map { media ->
			media.filter { mediaItem ->
                when(AwerySettings.adultContent.value) {
                    AwerySettings.AdultContent.SHOW -> true

                    AwerySettings.AdultContent.ONLY ->
                        mediaItem.ageRating?.let { AgeRating.of(it) } == AgeRating.NSFW

                    AwerySettings.AdultContent.HIDE ->
                        mediaItem.ageRating?.let { AgeRating.of(it) } != AgeRating.NSFW
                }
            }
        }.collectAsState(emptyList())
        
        val loadingStatus by viewModel.loadingStatus.collectAsState()
        
        LazyVerticalGrid(
            state = lazyGridState,
            modifier = Modifier.fillMaxSize(),

            contentPadding = contentPadding.add(
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),

            columns = GridCells.Adaptive(100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = if(media.isEmpty()) {
                Arrangement.Center
            } else Arrangement.spacedBy(16.dp, Alignment.Top)
        ) {
            singleItem("scrollFix")

            items(
                items = media,
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
            
            when(val status = loadingStatus) {
                LoadingStatus.Loading, LoadingStatus.NotInitialized -> {
                    singleItem("loading") {
                        LoadingIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                                .wrapContentSize(Alignment.Center)
                                .animateItem()
                        )
                    }
                }

                is LoadingStatus.Failed -> {
                    singleItem("failed") {
                        SelectionContainer {
                            InfoBox(
                                modifier = Modifier.animateItem(),
                                throwable = status.throwable
                            )
                        }
                    }
                }

                is LoadingStatus.Loaded if(media.isEmpty()) -> {
                    singleItem("empty") {
                        SelectionContainer {
                            InfoBox(
                                modifier = Modifier.animateItem(),
                                title = stringResource(Res.string.nothing_found),
                                message = stringResource(Res.string.no_media_found)
                            )
                        }
                    }
                }

				else -> {}
			}
        }
    }
}

class ExtensionSearchScreenViewModel(
    private val destination: Routes.ExtensionSearch
): ViewModel() {
    private var module: CatalogModule? = null
    private var job: Job? = null
    
    private val _filters = MutableStateFlow(destination.filters)
    val filters = _filters.asStateFlow()
    
    private val _loadingStatus = MutableStateFlow<LoadingStatus>(LoadingStatus.NotInitialized)
    val loadingStatus = _loadingStatus.asStateFlow()

    private val _media = MutableStateFlow(emptyList<Media>())
    val media = _media.asStateFlow()
    
    private var currentPage = 0
    
    init {
        loadMore()
    }
    
    fun applyFilters(filters: List<Preference<*>>) {
        job?.cancel()
        job = null
        
        viewModelScope.launch {
            _media.emit(emptyList())
            currentPage = 0
            _filters.emit(filters)
            _loadingStatus.emit(LoadingStatus.NotInitialized)
            loadMore()
        }
    }

    fun loadMore() {
        if(loadingStatus.value !is LoadingStatus.NotInitialized) return
        _loadingStatus.value = LoadingStatus.Loading
        
        job = viewModelScope.launchTryingSupervise(Dispatchers.Default, onCatch = {
            viewModelScope.launch {
                _loadingStatus.emit(LoadingStatus.Failed(it))
            }
        }) {
            if(module == null) {
                module = Extensions[destination.extensionId]!!.get<CatalogModule>()!!
            }

            if(_filters.value == null) {
                _filters.emit(module!!.getDefaultFilters())
            }

            module!!.search(_filters.value!!, currentPage).also { results ->
                // Sometimes api may return media items with the same idea due
                // to new content being added during the pagination.
                val newMedia = results.items.filter { item ->
                    media.value.none { it.id == item.id }
                }

                _media.emit(_media.value + newMedia)

                _loadingStatus.value = if(results.hasNextPage) {
                    LoadingStatus.NotInitialized
                } else LoadingStatus.Loaded

                if(results.hasNextPage) {
                    currentPage++

                    if(newMedia.isEmpty()) {
                        loadMore()
                    }
                }
            }
        }
    }
}