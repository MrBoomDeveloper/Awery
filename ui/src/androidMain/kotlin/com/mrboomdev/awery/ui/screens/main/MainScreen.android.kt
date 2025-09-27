package com.mrboomdev.awery.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_account_outlined
import com.mrboomdev.awery.resources.ic_settings_outlined
import com.mrboomdev.awery.resources.logo_awery
import com.mrboomdev.awery.ui.MainRoutes
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.FlexibleTopAppBar
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.TvIconButton
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.utils.saveFocus
import com.mrboomdev.awery.ui.utils.thenIf
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun MainScreen(viewModel: MainScreenViewModel) {
    if(Awery.isTv) TvMainScreen(viewModel)
    else DefaultMainScreen(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TvMainScreen(viewModel: MainScreenViewModel) { 
	val topBarBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val pagerState = rememberPagerState { MainRoutes.entries.size } 
	val coroutineScope = rememberCoroutineScope()
    val navigation = Navigation.current() 
	val toaster = LocalToaster.current

	val pagerFocusRequester = remember { FocusRequester() }

	Scaffold(
		modifier = Modifier
			.fillMaxSize()
			.nestedScroll(topBarBehavior.nestedScrollConnection),
		
		topBar = {
			FlexibleTopAppBar(
				modifier = Modifier.fillMaxWidth(),
				scrollBehavior = topBarBehavior,
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = Color.Transparent,
					scrolledContainerColor = Color.Transparent
				)
			) {
				Row(
					modifier = Modifier
						.padding(16.dp)
						.fillMaxWidth()
						.focusGroup(),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					val itemModifier = Modifier.focusProperties { down = pagerFocusRequester }
					val initialTabFocusRequester = remember { FocusRequester() }

					Image(
						modifier = Modifier.size(36.dp),
						painter = painterResource(Res.drawable.logo_awery),
						contentDescription = null
					)

					Text(
						style = MaterialTheme.typography.titleLarge,
						color = Color.White,
						text = "Awery"
					)

					TabRow(
						modifier = Modifier
							.weight(1f)
							.wrapContentSize(Alignment.Center)
							.focusRestorer(initialTabFocusRequester),
						selectedTabIndex = pagerState.currentPage
					) {
						MainRoutes.entries.forEachIndexed { index, tab ->
							if(tab.desktopOnly) return@forEachIndexed
							
							key(index) {
								Tab(
									modifier = itemModifier
										.thenIf(index == pagerState.currentPage) {
											focusRequester(initialTabFocusRequester)
										},

									selected = index == pagerState.currentPage,
									onClick = {},

									onFocus = {
										coroutineScope.launch {
											pagerState.animateScrollToPage(index)
										}
									}
								) {
									Text(
										modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
										text = stringResource(tab.title)
									)
								}
							}
						}
					}

					val buttonColors = IconButtonDefaults.colors(
						containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
						contentColor = MaterialTheme.colorScheme.onSecondaryContainer
					)

					TvIconButton(
						modifier = itemModifier.saveFocus()/*.focusRequester(headerFocusRequester)*/,
						painter = painterResource(Res.drawable.ic_settings_outlined),
						contentDescription = null,
						colors = buttonColors,
						onClick = { navigation.push(Routes.Settings()) }
					)

					TvIconButton(
						modifier = itemModifier,
						painter = painterResource(Res.drawable.ic_account_outlined),
						contentDescription = null,
						colors = buttonColors,
						onClick = {
							toaster.toast("Account page isn't done yet!", duration = 2500)
						}
					)
				}
			}
		}
	) { contentPadding ->
		Box(Modifier.fillMaxSize()) {
			AsyncImage(
				modifier = Modifier
					.fillMaxSize()
					.drawWithCache {
						val gradient = Brush.verticalGradient(listOf(
							Color.Black.copy(alpha = .5f),
							Color.Black.copy(alpha = .95f)
						))

						onDrawWithContent {
							drawContent()
							drawRect(gradient)
						}
					},

				model = ImageRequest.Builder(LocalPlatformContext.current)
					.addLastModifiedToFileCacheKey(true)
					.data(FileKit.filesDir / "wallpaper.png")
					.build(),

				contentDescription = null,
				contentScale = ContentScale.Crop
			)
			
			HorizontalPager(
				modifier = Modifier
					.fillMaxSize()
					.focusRequester(pagerFocusRequester)
				/*.focusProperties { up = headerFocusRequester }*/,
				state = pagerState,
				userScrollEnabled = false
			) { page ->
				when(page) {
					0 -> TvHomePage(viewModel, contentPadding)
					1 -> SearchPage(viewModel, contentPadding, "")
					2 -> NotificationsPage(contentPadding)
					3 -> LibraryPage(viewModel, contentPadding)
				}
			}
		}
	}
}