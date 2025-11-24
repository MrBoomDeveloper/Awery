package com.mrboomdev.awery.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.navigation.Routes
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.components.MediaCard
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.utils.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min

@Composable
fun LibraryTabbedScreen(
	viewModel: LibraryViewModel = viewModel { LibraryViewModel() },
	contentPadding: PaddingValues
) {
	val isNoLists by viewModel.isNoLists.collectAsState()
	val (didLoadLists, lists) = viewModel.lists.collectAsState().value
	val defaultTab by AwerySettings.libraryDefaultTab.collectAsState()
	val coroutineScope = rememberCoroutineScope()
	val navigation = Navigation.current()
	val pagerState = rememberPagerState { lists.size }

	RememberLaunchedEffect(lists) {
		lists.indexOfFirst {
			it.first.id == defaultTab
		}.takeUnless { it == -1 }?.also { 
			pagerState.scrollToPage(it)
		}
	}

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		AnimatedVisibility(lists.isNotEmpty()) {
			if(lists.isEmpty()) {
				// Then whole thing crashes while transitioning to fewer tabs when there were more.
				return@AnimatedVisibility
			}
			
			PrimaryScrollableTabRow(
				modifier = Modifier
					.padding(contentPadding.exclude(bottom = true)),
				containerColor = Color.Transparent,
				selectedTabIndex = min(lists.size, pagerState.currentPage),
				edgePadding = niceSideInset() - 16.dp,
				minTabWidth = 75.dp,
				divider = {}
			) {
				lists.forEachIndexed { index, (tab, _) ->
					Tab(
						modifier = Modifier.clip(RoundedCornerShape(16.dp)),
						selected = index == pagerState.currentPage,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(index)
							}
						}
					) {
						Text(
							modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
							fontFamily = AweryFonts.poppins,
							text = tab.name
						)
					}
				}
				
				Tab(
					modifier = Modifier.clip(RoundedCornerShape(16.dp)),
					selected = false,
					onClick = {
						navigation.push(Routes.Settings(
							initialPage = SettingsPages.Lists
						))
					}
				) {
					Icon(
						modifier = Modifier.size(24.dp),
						painter = painterResource(Res.drawable.ic_edit_outlined),
						contentDescription = null
					)
				}
			}
		}
		
		HorizontalDivider()
		
		Crossfade(when {
			isNoLists -> LibraryStatus.EMPTY
			!didLoadLists -> LibraryStatus.LOADING
			else -> LibraryStatus.LOADED
		}) { status ->
			when(status) {
				LibraryStatus.LOADING -> {
					CircularProgressIndicator(
						modifier = Modifier
							.fillMaxSize()
							.wrapContentSize()
					)
				}

				LibraryStatus.EMPTY -> {
					InfoBox(
						modifier = Modifier
							.fillMaxSize()
							.wrapContentSize(Alignment.Center),
						icon = painterResource(Res.drawable.ic_collections_bookmark_outlined),
						title = stringResource(Res.string.empty_library_title),
						message = stringResource(Res.string.empty_library_message)
					)
				}

				LibraryStatus.LOADED -> {
					HorizontalPager(
						modifier = Modifier.fillMaxSize(),
						state = pagerState,
						key = { it }
					) { index ->
						Crossfade(lists[index].second.isEmpty()) { isEmpty ->
							when(isEmpty) {
								true -> {
									InfoBox(
										modifier = Modifier
											.fillMaxSize()
											.wrapContentSize(Alignment.Center),
										icon = painterResource(Res.drawable.ic_collections_bookmark_outlined),
										title = "Empty list",
										message = stringResource(Res.string.empty_library_message)
									)
								}
								
								false -> {
									LazyVerticalGrid(
										modifier = Modifier.fillMaxSize(),

										contentPadding = contentPadding.only(
											horizontal = true, bottom = true
										).add(start = niceSideInset(), end = niceSideInset()),

										columns = GridCells.Adaptive(100.dp),
										horizontalArrangement = Arrangement.spacedBy(8.dp),
										verticalArrangement =  Arrangement.spacedBy(16.dp, Alignment.Top)
									) {
										singleItem("scrollFix")

										items(
											items = lists[index].second,
											key = { "${it.first.extensionId}_${it.first.id}" }
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
												modifier = Modifier.fillMaxWidth(),
												media = media,
												onClick = {
													navigation.push(Routes.Media(
														extensionId = dbMedia.extensionId,
														extensionName = null,
														media = media
													))
												},
												onLongClick = { showActionsDialog = true }
											)
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}