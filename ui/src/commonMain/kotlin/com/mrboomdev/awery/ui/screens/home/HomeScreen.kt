package com.mrboomdev.awery.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.shimmer
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.util.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
	viewModel: HomeViewModel = viewModel { HomeViewModel() },
	contentPadding: PaddingValues
) {
	val isReloading by viewModel.isReloading.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val username by AwerySettings.username.collectAsState()
	val coroutineScope = rememberCoroutineScope()
	val lazyListState = rememberLazyListState()
	val navigation = Navigation.current()
	
	val showShimmer = isLoading && !isReloading && viewModel.loadedFeeds.isEmpty()

	PostLaunchedEffect(AwerySettings.adultContent.collectAsState().value) {
		viewModel.reload()
	}
	
	@Composable
	fun Modifier.shimmer() = placeholder(
		visible = true,
		highlight = PlaceholderHighlight.shimmer(
			highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest
		)
	)
	
	@Composable
	fun Content() {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding.only(vertical = true),
			state = lazyListState,
			userScrollEnabled = !showShimmer
		) { 
			singleItem("header") {
				Column(
					modifier = Modifier
						.padding(contentPadding.only(horizontal = true))
						.padding(
							top = if(Awery.platform == Platform.DESKTOP) 32.dp else 8.dp, 
							bottom = if(!showShimmer) 16.dp else 0.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Text(
						modifier = Modifier.padding(horizontal = niceSideInset()),
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Normal,
						text = "Welcome back, $username!"
					)
					
					Text(
						modifier = Modifier.padding(horizontal = niceSideInset()),
						color = MaterialTheme.colorScheme.secondary,
						text = remember {
							@OptIn(ExperimentalTime::class)
							val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
							
							val greetings = listOf(
								"Sit back, relax, and press play. Your entertainment is waiting.",
								"Your cinematic escape starts now.",
								"Leave the world behind for a while.",
								"Time to unwind and enjoy the show.",
								"Discover your next favorite story.",
								"Just press play.",
								"Happy watching",
								"The ultimate collection for your viewing pleasure.",
								"Your comfort zone is calling.",
								"Settle in. Your show is about to begin.",
								"This is your time to relax.",
								"Let the outside world fade.",
								"Ready to explore the collection?",
								"A universe of entertainment at your fingertips.",
								"Let's find something you'll love.",
								"So much to watch, where to begin?",
								"There's always something new to find.",
								"Lights, camera, and you're in action.",
								"Grab the popcorn, it's showtime"
							)
								
							val nightGreetings = listOf(
								"What will it be tonight?",
								"Let the stress of the day melt away.",
								"A well-deserved break awaits.",
								"The night is just getting started",
								"Your late-night destination."
							)
								
							(if(currentHour !in 4..18) {
								greetings + nightGreetings
							} else greetings).random()
						}
					)
				}
			}

			items(
				items = viewModel.loadedFeeds,
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
						} }.animateItem(),

					contentPadding = contentPadding.only(horizontal = true)
						.add(horizontal = niceSideInset(), vertical = 8.dp),

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

			if(!showShimmer && isLoading) {
				singleItem("loadingIndicator") {
					LoadingIndicator(
						modifier = Modifier
							.fillMaxWidth()
							.wrapContentSize(Alignment.Center)
							.padding(vertical = 64.dp)
							.animateItem()
					)
				}
			}

			if(!showShimmer) {
				items(
					items = viewModel.failedFeeds,
					key = { "failedFeed_${it.first.id}_${it.second.id}" },
					contentType = { "failedFeed" }
				) { (extension, feed, throwable) ->
					var isReloading by remember(extension, feed, throwable) { mutableStateOf(false) }

					FeedRow(
						modifier = Modifier
							.fillMaxWidth()
							.animateItem(),

						contentPadding = contentPadding.only(horizontal = true)
							.add(horizontal = niceSideInset(), vertical = 8.dp),

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
												lazyListState.animateScrollToItem(feedIndex + 1)
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
			
			if(showShimmer) {
				items(
					count = 99,
					contentType = { "shimmer" }
				) {
					Column(
						modifier = Modifier 
							.padding(contentPadding.only(start = true))
							.padding(start = niceSideInset(), top = 32.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) { 
						Text(
							modifier = Modifier.shimmer(),
							style = MaterialTheme.typography.titleLarge,
							text = "My awesome feed name"
						)

						LazyRow(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							userScrollEnabled = false
						) { 
							items(99) {
								Column(
									modifier = Modifier.width(115.dp),
									verticalArrangement = Arrangement.spacedBy(8.dp)
								) {
									Spacer(
										modifier = Modifier
											.shimmer()
											.fillMaxWidth()
											.aspectRatio(9f / 12f)
									)

									Text(
										modifier = Modifier.shimmer(),
										style = MaterialTheme.typography.bodyMedium,
										overflow = TextOverflow.Ellipsis,
										minLines = 2,
										maxLines = 2,
										text = "My favourite hentai"
									)
								}
							}
						}
					}
				}
			}
		}
	}
	
	if(Awery.platform == Platform.DESKTOP) {
		Content()
	} else {
		PullToRefreshBox(
			modifier = Modifier.fillMaxSize(),
			isRefreshing = isReloading,
			onRefresh = { viewModel.reload() },
			content = { Content() },
//			indicator = {
//				Indicator(
//					state = rememberPullToRefreshState(),
//					modifier = Modifier.align(Alignment.TopCenter),
//					color = MaterialTheme.colorScheme.primary,
//					isRefreshing = isReloading
//				)
//			}
		)
	}
}