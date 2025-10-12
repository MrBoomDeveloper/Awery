package com.mrboomdev.awery.ui.screens.media

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mrboomdev.awery.extension.loaders.getBanner
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.FlexibleTopAppBar
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.utils.collapse
import com.mrboomdev.awery.ui.utils.transparentTopAppBarColors
import kotlinx.coroutines.launch

@Composable
actual fun MediaScreen(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel,
	contentPadding: PaddingValues
) = DesktopMediaScreen(destination, viewModel)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DesktopMediaScreen(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel
) {
	val media by viewModel.media.collectAsState()
	
    val tabs = remember(media) {
        MediaScreenTabs.getVisibleFor(media)
    }

    val infoHeaderBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(snapAnimationSpec = null)
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { tabs.count() }
    val toaster = LocalToaster.current

    fun openWatchPage() {
        if(media.type == Media.Type.READABLE) {
            toaster.toast("Reading isn't supported yet!")
            return
        }

        coroutineScope.launch {
            launch {
                pagerState.animateScrollToPage(tabs.indexOf(MediaScreenTabs.EPISODES))
            }

            launch {
                infoHeaderBehavior.collapse()
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .widthIn(max = 1000.dp)
                .nestedScroll(infoHeaderBehavior.nestedScrollConnection),

            containerColor = Color.Transparent,

            topBar = {
				val banner = media.getBanner()
				var isBannerFuckedUp by remember(banner) { mutableStateOf(banner == null) }
				
                FlexibleTopAppBar(
					scrollBehavior = infoHeaderBehavior,
					colors = TopAppBarDefaults.transparentTopAppBarColors()
				) {
					if(isBannerFuckedUp) {
						Spacer(Modifier.height(32.dp))
						return@FlexibleTopAppBar
					}
					
					AsyncImage(
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = 8.dp)
							.clip(RoundedCornerShape(16.dp))
							.background(MaterialTheme.colorScheme.surface)
							.animateContentSize(),
						model = banner,
						contentDescription = null,
						alpha = .5f,
						onError = { isBannerFuckedUp = true },
						onLoading = { isBannerFuckedUp = false },
						onSuccess = { (_, result) ->
							isBannerFuckedUp = result.image.let { it.height / it.width } >= 1
						}
					)
                }
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
					.padding(top = 16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    media.getPoster()?.also { poster ->
                        AsyncImage(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .fillMaxWidth(.3f)
                                .animateContentSize(),
                            model = poster,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Column {
                        MediaScreenActions(
                            destination = destination,
                            viewModel = viewModel,
                            alignAtCenter = false,
                            stretchButtons = false,
                            onWatch = ::openWatchPage
                        )

                        MediaScreenContent(
                            media = media,
							extensionId = destination.extensionId,
                            watcher = viewModel.watcher.collectAsState().value,
                            pagerState = pagerState,
                            tabs = tabs,
                            coroutineScope = coroutineScope,
                            contentPadding = PaddingValues.Zero
                        )
                    }
                }
            }
        }
    }
}