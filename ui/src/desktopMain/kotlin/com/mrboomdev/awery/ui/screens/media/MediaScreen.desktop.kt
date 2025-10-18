package com.mrboomdev.awery.ui.screens.media

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.mrboomdev.awery.extension.loaders.getBanner
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.screens.GalleryScreen
import com.mrboomdev.awery.ui.utils.collapse
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
	var showGallery by rememberSaveable { mutableStateOf(false) }
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

	if(showGallery) {
		Dialog(
			onDismissRequest = { showGallery = false },
			properties = DialogProperties(
				usePlatformDefaultWidth = false
			)
		) {
			GalleryScreen(
				onDismissRequest = { showGallery = false },
				elements = listOfNotNull(
					media.largePoster,
					media.poster,
					media.banner
				)
			)
		}
	}
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
		AsyncImage(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.background)
				.fillMaxSize(),
			model = media.getBanner(),
			contentScale = ContentScale.Crop,
			alpha = .15f,
			contentDescription = null
		)
		
		Column(
			modifier = Modifier
				.padding(top = 16.dp, start = 16.dp, end = 16.dp)
				.widthIn(max = 1000.dp)
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
							.clickable { showGallery = true }
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