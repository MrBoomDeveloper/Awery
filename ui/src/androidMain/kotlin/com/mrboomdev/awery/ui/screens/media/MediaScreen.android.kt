package com.mrboomdev.awery.ui.screens.media

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.SuggestionChip
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import be.digitalia.compose.htmlconverter.htmlToString
import coil3.compose.AsyncImage
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.LoadingStatus
import com.mrboomdev.awery.core.utils.replaceAll
import com.mrboomdev.awery.core.utils.toCalendar
import com.mrboomdev.awery.extension.loaders.getBanner
import com.mrboomdev.awery.extension.loaders.getLargePoster
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.bookmark
import com.mrboomdev.awery.resources.chapters
import com.mrboomdev.awery.resources.episodes
import com.mrboomdev.awery.resources.ic_book_filled
import com.mrboomdev.awery.resources.ic_bookmark_filled
import com.mrboomdev.awery.resources.ic_play_filled
import com.mrboomdev.awery.resources.read_now
import com.mrboomdev.awery.resources.tags
import com.mrboomdev.awery.resources.watch_now
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.effects.BackEffect
import com.mrboomdev.awery.ui.popups.BookmarkMediaDialog
import com.mrboomdev.awery.ui.utils.CustomBringIntoViewSpec
import com.mrboomdev.awery.ui.utils.classify
import com.mrboomdev.awery.ui.utils.formatAsCountry
import com.mrboomdev.awery.ui.utils.handleDPadKeyEvents
import com.mrboomdev.awery.ui.utils.singleItem
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

private val SHADOW_COLOR = Color(0xee000000)

@Composable
actual fun MediaScreen(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel
) {
    if(Awery.isTv) TvMediaScreen(destination, viewModel)
    else DefaultMediaScreen(destination, viewModel)
}

@Composable
private fun TvMediaScreen(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    
    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            modifier = Modifier.matchParentSize(),
            model = viewModel.media.getBanner(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            when(page) {
                0 -> MainPage(
                    destination = destination,
                    viewModel = viewModel,
                    onOpenPage = { coroutineScope.launch { pagerState.animateScrollToPage(it) } }
                )
                
                1 -> EpisodesPage(
                    viewModel = viewModel,
                    onBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                )
            }
        }
    }
}

@Composable
private fun StretchyOvalGradient(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        // Calculate the aspect ratio of the component.
        val aspectRatio = maxWidth / maxHeight

        // The core of the stretching effect.
        // We create a circular gradient and then scale the Box it's in.
        // The scaling is non-uniform, stretching the circle into an oval.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Apply the scaling.
                // We use maxOf to ensure we are always scaling up to fill the space.
                .scale(
                    scaleX = maxOf(aspectRatio, 1f),
                    scaleY = maxOf(1f / aspectRatio, 1f)
                )
        ) {
            drawRect(Brush.radialGradient(
                colors = listOf(
                    Color.Transparent, 
                    Color(0xaa000000),
                    SHADOW_COLOR
                ),
                
                radius = size.width / 2,
                center = Offset(size.width / 5 * 3, 0f)
            ))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun MainPage(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel,
    onOpenPage: (Int) -> Unit
) {
    val windowHeight = LocalWindowInfo.current.containerSize.height
    val mainSectionHeight = with(LocalDensity.current) { (windowHeight * .9f).toDp() }
    val spaceHeight = with(LocalDensity.current) { (windowHeight * .2f).toDp() }
    val coroutineScope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    
    @Composable
    fun GeneralSection(
        infoWidthFraction: Float
    ) {
        BasicText(
            modifier = Modifier
                .fillMaxWidth(infoWidthFraction)
                .heightIn(max = 200.dp),

            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Light,
                color = Color.White
            ),

            autoSize = TextAutoSize.StepBased(
                maxFontSize = 57.sp
            ),

            text = viewModel.media.title
        )

        listOfNotNull(
            destination.extensionName,
            viewModel.media.ageRating,
            viewModel.media.releaseDate?.toCalendar()?.get(Calendar.YEAR),
            viewModel.media.country?.formatAsCountry(),

            viewModel.media.episodes?.let { episodes ->
                pluralStringResource(when(viewModel.media.type) {
                    Media.Type.WATCHABLE -> Res.plurals.episodes
                    Media.Type.READABLE -> Res.plurals.chapters
                }, episodes, episodes)
            },

            viewModel.media.tags?.takeIf { it.isNotEmpty() }?.joinToString(", ")
        ).joinToString(" • ").takeIf { it.isNotBlank() }?.also { meta ->
            Text(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .fillMaxWidth(infoWidthFraction),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = meta
            )
        }

        viewModel.media.description?.also { description ->
            var expand by remember { mutableStateOf(false) }

            if(expand) {
                Dialog(
                    onDismissRequest = { expand = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    val scrollState = rememberScrollState()
                    
                    Text(
                        modifier = Modifier
                            .background(Color(0xbb000000))
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                            .verticalScroll(scrollState)
                            .padding(64.dp)
                            .focusable()
                            .handleDPadKeyEvents(
                                onUp = {
                                    coroutineScope.launch { 
                                        scrollState.animateScrollBy(-50f)
                                    }
                                },
                                
                                onDown = {
                                    coroutineScope.launch {
                                        scrollState.animateScrollBy(50f)
                                    }
                                }
                            ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        text = remember(description) {
                            htmlToAnnotatedString(description, compactMode = true)
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(infoWidthFraction * .8f)
                    .wrapContentSize(),

                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    focusedContainerColor = Color(0x11d2d2d2),
                    focusedContentColor = Color.White,
                    pressedContainerColor = Color.White,
                    pressedContentColor = Color.Black
                ),

                scale = ClickableSurfaceDefaults.scale(
                    focusedScale = 1.025f
                ),

                onClick = { expand = true }
            ) {
                Text(
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    text = remember(description) {
                        htmlToString(description, compactMode = true)
                            .replaceAll("\n\n", "\n")
                            .trim()
                    }
                )
            }
        }

        val focusRequester = remember { FocusRequester() }
        var showBookmarkDialog by remember { mutableStateOf(false) }
        
        if(showBookmarkDialog) {
            BookmarkMediaDialog(
                extensionId = destination.extensionId,
                media = viewModel.media,
                onDismissRequest = { showBookmarkDialog = false }
            )
        }
        
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .focusGroup()
                .focusRestorer(focusRequester),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.focusRequester(focusRequester),

                onClick = {
                    if(viewModel.media.type == Media.Type.READABLE) {
                        toaster.toast("Reading isn't supported yet!")
                        return@Button
                    }

                    onOpenPage(1)
                }
            ) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    contentDescription = null,
                    painter = painterResource(when(viewModel.media.type) {
                        Media.Type.WATCHABLE -> Res.drawable.ic_play_filled
                        Media.Type.READABLE -> Res.drawable.ic_book_filled
                    })
                )

                Spacer(Modifier.width(ButtonDefaults.IconSpacing))

                Text(stringResource(when(viewModel.media.type) {
                    Media.Type.WATCHABLE -> Res.string.watch_now
                    Media.Type.READABLE -> Res.string.read_now
                }))
            }

            Button({ showBookmarkDialog = true }) {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    contentDescription = null,
                    painter = painterResource(Res.drawable.ic_bookmark_filled)
                )

                Spacer(Modifier.width(ButtonDefaults.IconSpacing))

                Text(stringResource(Res.string.bookmark))
            }
        }
    }
    
    @Composable
    fun MoreInfoSection() {
        viewModel.media.tags?.also { tags ->
            Column(
                modifier = Modifier
                    .background(SHADOW_COLOR)
                    .padding(vertical = 8.dp, horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) { 
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    text = stringResource(Res.string.tags)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        SuggestionChip(
                            onClick = {}
                        ) {
                            Text(tag)
                        }
                    }
                }
            }
        }
    }
    
    var isHeroFocused by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    
    val bringIntoViewSpec = remember(isHeroFocused) {
        if(isHeroFocused) CustomBringIntoViewSpec(1f, 1f)
        else CustomBringIntoViewSpec(.5f, .5f)
    }
    
    LaunchedEffect(isHeroFocused) {
        scrollState.animateScrollTo(
            if(isHeroFocused) 0 else windowHeight / 2
        )
    }

    CompositionLocalProvider(
        LocalBringIntoViewSpec provides bringIntoViewSpec
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box {
                StretchyOvalGradient(Modifier.matchParentSize())
                
                if(viewModel.isUpdatingMedia.collectAsState().value) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                Row(
                    modifier = Modifier
                        .height(mainSectionHeight)
                        .padding(horizontal = 64.dp)
                        .onFocusChanged { isHeroFocused = it.hasFocus },
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    val noBannerButPoster = viewModel.media.banner == null && 
                            (viewModel.media.poster != null || viewModel.media.largePoster != null)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if(noBannerButPoster) .6f else 1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
                    ) {
                        MaterialTheme(colorScheme = darkColorScheme()) {
                            GeneralSection(infoWidthFraction = if(noBannerButPoster) 1f else .6f)
                        }
                    }

                    if(noBannerButPoster) {
                        var showPoster by remember(viewModel.media) { mutableStateOf(true) }
                        
                        if(showPoster) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .padding(top = 32.dp, end = 32.dp, bottom = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .animateContentSize(),

                                    model = viewModel.media.getLargePoster(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    onSuccess = { state ->
                                        if(state.result.image.let { it.width / it.height } >= 1) {
                                            // THIS IS A FUCKING BANNER! NOT A POSTER!
                                            // THIS SHIT SIMPLY WONT FIT IN THE LAYOUT!
                                            showPoster = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            MoreInfoSection()

            Spacer(
                modifier = Modifier
                    .background(SHADOW_COLOR)
                    .height(spaceHeight)
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodesPage(
    viewModel: MediaScreenViewModel,
    onBack: () -> Unit
) {
    val windowWidth = LocalWindowInfo.current.containerSize.width
    val windowWidthDp = with(LocalDensity.current) { windowWidth.toDp() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loadEpisodes()
    }
    
    BackEffect(onBack)

    CompositionLocalProvider(
        LocalBringIntoViewSpec provides CustomBringIntoViewSpec(0f, 0f)
    ) {
        LazyRow(
            modifier = Modifier
                .background(SHADOW_COLOR)
                .fillMaxSize()
//                .focusable()
                .handleDPadKeyEvents(
                    onUp = {},
                    onDown = {},
                    onLeft = {},
                    onRight = {}
                )
        ) {
            singleItem("scrollFixer")
            
//            items(
//                items = viewModel.watchVariants
//            ) { watchVariants ->
//                CompositionLocalProvider(
//                    LocalBringIntoViewSpec provides CustomBringIntoViewSpec(.5f, .5f)
//                ) {
//                    LazyColumn(
//                        modifier = Modifier
//                            .width(windowWidthDp / 2)
//                            .animateItem(),
//                        contentPadding = PaddingValues(32.dp)
//                    ) {
//                        items(
//                            items = watchVariants
//                        ) { watchVariant ->
//                            Card(
//                                modifier = Modifier.fillMaxWidth(),
//                                onClick = {}
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .background(Color(0x22ffffff))
//                                        .padding(horizontal = 16.dp, vertical = 8.dp)
//                                ) {
////                                    AsyncImage(
////                                        model = watchVariant.thumbnail,
////                                        contentDescription = null
////                                    )
//                                    
//                                    Column(
//                                        verticalArrangement = Arrangement.spacedBy(4.dp)
//                                    ) {
//                                        Text(
//                                            style = MaterialTheme.typography.titleLarge,
//                                            color = Color.White,
//                                            text = watchVariant.title
//                                        ) 
//                                        
//                                        watchVariant.releaseDate?.also { releaseDate ->
//                                            Text(
//                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                                text = HumanReadable.timeAgo(Clock.System.now() - releaseDate.milliseconds)
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            
            when(val status = viewModel.episodesLoadingStatus) {
				is LoadingStatus.Failed -> {
                    singleItem("failed") { 
                        Column(
                            modifier = Modifier
//                                .width(if(viewModel.watchVariants.isEmpty()) windowWidthDp else windowWidthDp / 2)
                                .fillMaxHeight()
                                .wrapContentSize(Alignment.Center)
                                .padding(64.dp)
                                .animateItem(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) { 
                            Text(
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                text = "Failed to load episodes"
                            )
                            
                            Text(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                text = status.throwable.classify().message
                            )
                            
                            val focusRequester = remember { FocusRequester() }
                            
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                            
                            Button(
                                modifier = Modifier.focusRequester(focusRequester),
                                onClick = { viewModel.loadEpisodes() }
                            ) {
                                Text("Try again")
                            }
                        }
                    }
                }
                
				LoadingStatus.Loading, LoadingStatus.NotInitialized -> {
                    singleItem("progressIndicator") {
                        CircularProgressIndicator(
                            modifier = Modifier
//                                .width(if(viewModel.watchVariants.isEmpty()) windowWidthDp else windowWidthDp / 2)
                                .fillMaxHeight()
                                .wrapContentSize(Alignment.Center)
                                .animateItem()
                        )
                    }
                }
                
                LoadingStatus.Loaded -> {}
			}
        }
    }
}