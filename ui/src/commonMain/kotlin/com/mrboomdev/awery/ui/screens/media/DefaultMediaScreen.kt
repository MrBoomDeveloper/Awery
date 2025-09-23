package com.mrboomdev.awery.ui.screens.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.HorizontalRuler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State.Empty.painter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.await
import com.mrboomdev.awery.core.utils.iterate
import com.mrboomdev.awery.core.utils.iterateIndexed
import com.mrboomdev.awery.core.utils.iterateMutable
import com.mrboomdev.awery.core.utils.removeAll
import com.mrboomdev.awery.core.utils.removeAllNext
import com.mrboomdev.awery.core.utils.retryUntilSuccess
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.cachedModules
import com.mrboomdev.awery.extension.loaders.getBanner
import com.mrboomdev.awery.extension.loaders.getLargePoster
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.loaders.watch.VariantWatcher
import com.mrboomdev.awery.extension.loaders.watch.WatcherNode
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.WatchVariant
import com.mrboomdev.awery.extension.sdk.modules.WatchModule
import com.mrboomdev.awery.resources.AweryFonts
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.ic_block
import com.mrboomdev.awery.resources.ic_share_filled
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.Breadcrumb
import com.mrboomdev.awery.ui.components.ExpandableText
import com.mrboomdev.awery.ui.components.FlexibleTopAppBar
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.effects.BackEffect
import com.mrboomdev.awery.ui.theme.SeedAweryTheme
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.collapse
import com.mrboomdev.awery.ui.utils.currentWindowHeight
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.awery.ui.utils.exclude
import com.mrboomdev.awery.ui.utils.only
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.stateListSaver
import com.mrboomdev.awery.ui.utils.top
import com.mrboomdev.navigation.core.safePop
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import eu.kanade.tachiyomi.source.model.Page
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.jetbrains.compose.resources.painterResource
import java.awt.SystemColor.text

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DefaultMediaScreen(
	destination: Routes.Media,
	viewModel: MediaScreenViewModel
) {
	val topBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	val infoHeaderBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(snapAnimationSpec = null)
	val coroutineScope = rememberCoroutineScope()
	val windowSize = currentWindowSize()
	val toaster = LocalToaster.current
	val navigation = Navigation.current()

	val tabs = remember(viewModel.media) {
		MediaScreenTabs.getVisibleFor(viewModel.media)
	}

	val pagerState = rememberPagerState { tabs.count() }

	val defaultColor = remember(viewModel.media) {
		if(viewModel.media.getLargePoster() == null) return@remember null

		runBlocking {
			colorsCache[destination.extensionId + destination.media.id]
		}
	}

	val dominantColorState = rememberDominantColorState(
		defaultColor = defaultColor?.color ?: MaterialTheme.colorScheme.primary,
		defaultOnColor = defaultColor?.onColor ?: MaterialTheme.colorScheme.onPrimary,
		loader = rememberNetworkLoader()
	)
	
	fun openWatchPage() {
		if(viewModel.media.type == Media.Type.READABLE) {
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

	LaunchedEffect(viewModel.media) {
		viewModel.media.getLargePoster()?.also { poster ->
			dominantColorState.updateFrom(Url(poster))
			
			colorsCache[destination.extensionId + destination.media.id] = 
				ColorScheme(dominantColorState.color, dominantColorState.onColor)
		}
	}

	SeedAweryTheme(seedColor = dominantColorState.color) {
		@Composable
		fun TopButton(
			modifier: Modifier = Modifier,
			padding: Dp = 3.dp,
			painter: Painter,
			onClick: () -> Unit
		) {
			SmallFloatingActionButton(
				modifier = modifier.size(40.dp),
				containerColor = FloatingActionButtonDefaults.containerColor.copy(alpha = .5f),
				contentColor = MaterialTheme.colorScheme.primary,
				onClick = onClick
			) {
				Icon(
					modifier = Modifier
						.size(48.dp)
						.padding(padding),
					painter = painter,
					contentDescription = null
				)
			}
		}

		@Composable
		fun Portrait() {
			val topBarColor by animateColorAsState(
				animationSpec = tween(500),
				targetValue = if(infoHeaderBehavior.state.heightOffset < -100f) {
					MaterialTheme.colorScheme.surface
				} else Color.Transparent,
			)

			Scaffold(
				modifier = Modifier
					.fillMaxSize()
					.nestedScroll(topBarBehavior.nestedScrollConnection)
					.nestedScroll(infoHeaderBehavior.nestedScrollConnection),

				topBar = {
					FlexibleTopAppBar(
						scrollBehavior = infoHeaderBehavior,
						colors = TopAppBarDefaults.topAppBarColors(
							containerColor = Color.Transparent,
							scrolledContainerColor = Color.Transparent
						)
					) {
						Box {
							var didFailToLoadPoster by remember(viewModel.media.getPoster()) {
								mutableStateOf(viewModel.media.getLargePoster() == null)
							}

							var didLoadPoster by remember(viewModel.media.getPoster()) { mutableStateOf(false) }
							var isPosterFuckedUp by remember(viewModel.media.getPoster()) { mutableStateOf(false) }

							viewModel.media.getBanner()?.also { banner ->
								Box(Modifier.matchParentSize()) {
									val alpha by animateFloatAsState(if(isPosterFuckedUp) .5f else .25f)

									AsyncImage(
										modifier = Modifier
											.alpha(alpha)
											.hazeEffect(HazeStyle(blurRadius = 1.dp, tint = null))
											.fillMaxSize(),
										model = banner,
										contentDescription = null,
										contentScale = ContentScale.Crop
									)

									val shadowBrush = Brush.verticalGradient(
										0f to Color.Transparent,
										1f to MaterialTheme.colorScheme.surface
									)

									Canvas(
										modifier = Modifier
											.fillMaxWidth()
											.fillMaxHeight(.5f)
											.align(Alignment.BottomCenter)
									) {
										drawRect(shadowBrush)
									}
								}
							}

							Column(
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = 16.dp, vertical = 8.dp)
									.windowInsetsPadding(WindowInsets.safeDrawing.only(
										WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
							) {
								Spacer(Modifier.height(animateDpAsState(when {
									didLoadPoster && isPosterFuckedUp -> 48.dp
									didFailToLoadPoster -> 56.dp
									else -> 16.dp
								}, tween()).value))
								
								viewModel.media.getLargePoster()?.also { poster ->
									if(isPosterFuckedUp) {
										AsyncImage(
											modifier = Modifier
												.padding(top = 8.dp, bottom = 16.dp)
												.padding(horizontal = 8.dp)
												.clip(RoundedCornerShape(16.dp))
												.fillMaxWidth()
												.animateContentSize(),
											model = poster,
											contentDescription = null
										)

										return@also
									}

									AsyncImage(
										modifier = Modifier
											.clip(RoundedCornerShape(16.dp))
											.padding(horizontal = 56.dp)
											.heightIn(max = currentWindowHeight() - 250.dp)
											.fillMaxWidth()
											.animateContentSize(),

										model = poster.let {
											ImageRequest.Builder(LocalPlatformContext.current)
												.placeholderMemoryCacheKey(it)
												.memoryCacheKey(it)
												.data(it)
												.build() 
										},

										contentDescription = null,
										
										onError = {
											didFailToLoadPoster = true 
										},
										
										onLoading = { 
											isPosterFuckedUp = false 
											didFailToLoadPoster = false
										},
										
										onSuccess = { (_, result) ->
											didLoadPoster = true
											isPosterFuckedUp = result.image.let { it.width / it.height } >= 1
										}
									)
								}

								Spacer(Modifier.height(animateDpAsState(when {
									didFailToLoadPoster || isPosterFuckedUp -> 0.dp
									else -> 12.dp
								}, tween()).value))

								MediaScreenActions(
									destination = destination,
									viewModel = viewModel,
									alignAtCenter = true,
									stretchButtons = true,
									onWatch = ::openWatchPage
								)
							}
						}
					}

					TopAppBar(
						scrollBehavior = topBarBehavior,

						navigationIcon = {
							TopButton(
								modifier = Modifier.padding(start = 8.dp),
								painter = painterResource(Res.drawable.ic_back),
								onClick = { navigation.safePop() }
							)
						},

						actions = {
							viewModel.media.url?.also { url ->
								TopButton(
									modifier = Modifier.padding(end = 8.dp),
									padding = 9.dp,
									painter = painterResource(Res.drawable.ic_share_filled),
									onClick = { Awery.share(url) }
								)
							}
						},

						title = {},
						colors = TopAppBarDefaults.topAppBarColors(
							containerColor = topBarColor,
							scrolledContainerColor = topBarColor
						)
					)
				}
			) { contentPadding ->
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(contentPadding.exclude(bottom = true))
				) {
					MediaScreenContent(
						media = viewModel.media,
						watcher = viewModel.watcher,
						pagerState = pagerState,
						tabs = tabs,
						coroutineScope = coroutineScope,
						contentPadding = contentPadding.only(bottom = true)
					)
				}
			}
		}

		@Composable
		fun Landscape() {
			Box(Modifier.fillMaxSize()) {
				viewModel.media.getBanner().also { banner ->
					Canvas(Modifier.fillMaxSize()) {
						drawRect(Color.Black)
					}

					AsyncImage(
						modifier = Modifier
							.alpha(.2f)
							.hazeEffect(HazeStyle(blurRadius = 1.dp, tint = null))
							.fillMaxSize(),
						model = banner,
						contentDescription = null,
						contentScale = ContentScale.Crop
					)

					val shadowBrush = Brush.verticalGradient(
						0f to Color.Transparent,
						1f to MaterialTheme.colorScheme.surface
					)

					Canvas(Modifier.fillMaxSize()) {
						drawRect(shadowBrush)
					}
				}

				Row(
					modifier = Modifier.fillMaxSize(),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Column(
						modifier = Modifier
							.padding(8.dp)
							.windowInsetsPadding(WindowInsets.safeDrawing.only(
								WindowInsetsSides.Vertical + WindowInsetsSides.Start)),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						TopButton(
							painter = painterResource(Res.drawable.ic_back),
							onClick = { navigation.safePop() }
						)

						viewModel.media.url?.also { url ->
							TopButton(
								padding = 9.dp,
								painter = painterResource(Res.drawable.ic_share_filled),
								onClick = { Awery.share(url) }
							)
						}
					}

					var showPoster by remember(viewModel.media) { mutableStateOf(true) }
					viewModel.media.getLargePoster()?.also { poster ->
						if(!showPoster || windowSize.width <= WindowSizeType.Medium) return@also

						Box(
							modifier = Modifier
								.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
								.padding(top = 8.dp, bottom = 16.dp)
						) {
							AsyncImage(
								modifier = Modifier
									.clip(RoundedCornerShape(16.dp))
									.fillMaxHeight()
									.animateContentSize(),

								model = poster.let {
									ImageRequest.Builder(LocalPlatformContext.current)
										.placeholderMemoryCacheKey(it)
										.memoryCacheKey(it)
										.data(it)
										.build()
								},

								contentDescription = null,
								onSuccess = { state ->
									if(state.result.image.let { it.width / it.height } >= 1) {
										// THIS IS A FUCKING BANNER! NOT AN POSTER!
										// THIS SHIT SIMPLY WON'T FIT IN THE LAYOUT!
										showPoster = false
									}
								}
							)
						}
					}

					Scaffold(
						modifier = Modifier
							.fillMaxHeight()
							.weight(1f)
							.nestedScroll(infoHeaderBehavior.nestedScrollConnection),

						topBar = {
							FlexibleTopAppBar(
								scrollBehavior = infoHeaderBehavior,
								colors = TopAppBarDefaults.topAppBarColors(
									containerColor = Color.Transparent,
									scrolledContainerColor = Color.Transparent
								)
							) {
								Column(
									modifier = Modifier
										.windowInsetsPadding(WindowInsets.safeDrawing.only(
											WindowInsetsSides.Top + WindowInsetsSides.End))
										.padding(horizontal = 16.dp)
										.padding(top = 16.dp)
								) {
									MediaScreenActions(
										destination = destination,
										viewModel = viewModel,
										alignAtCenter = false,
										stretchButtons = false,
										onWatch = ::openWatchPage
									)
								}
							}
						},

						containerColor = Color.Transparent
					) { contentPadding ->
						Column(
							modifier = Modifier
								.padding(contentPadding.exclude(start = true, end = true, bottom = true))
								.fillMaxSize()
						) {
							Spacer(Modifier.height(animateDpAsState(
								if(infoHeaderBehavior.state.let { it.heightOffset <= it.heightOffsetLimit }) {
									WindowInsets.safeDrawing.top
								} else 8.dp,
								animationSpec = tween(500)
							).value))

							MediaScreenContent(
								media = viewModel.media,
								watcher = viewModel.watcher,
								pagerState = pagerState,
								tabs = tabs,
								coroutineScope = coroutineScope,
								contentPadding = contentPadding.only(end = true, bottom = true)
							)
						}
					}
				}
			}
		}

		Box(Modifier.fillMaxSize()) {
			if(windowSize.width >= WindowSizeType.Large || windowSize.height <= WindowSizeType.Small) {
				Landscape()
			} else Portrait()
			
			if(viewModel.isUpdatingMedia.collectAsState().value) {
				LinearProgressIndicator(Modifier.fillMaxWidth())
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ColumnScope.MediaScreenContent(
	media: Media,
	watcher: WatcherNode,
	pagerState: PagerState,
	tabs: List<MediaScreenTabs>,
	coroutineScope: CoroutineScope,
	contentPadding: PaddingValues
) {
	val navigation = Navigation.current()
	
	if(tabs.isEmpty()) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(32.dp)
				.padding(contentPadding)
				.verticalScroll(rememberScrollState()),
			contentAlignment = Alignment.Center
		) {
			Text("No info can be shown.")
		}

		return
	}

	@Composable
	fun Tabs() {
		tabs.forEachIndexed { index, tab ->
			Tab(
				selected = index == pagerState.currentPage,
				onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
			) {
				Text(
					modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
					text = tab.getTitle(media)
				)
			}
		}
	}

	if(tabs.size > 1) {
		if(tabs.size > 3) {
			PrimaryScrollableTabRow(
				modifier = Modifier.verticalScroll(rememberScrollState()),
				edgePadding = 8.dp,
				selectedTabIndex = pagerState.currentPage,
				containerColor = Color.Transparent,
				divider = {}, // It doesn't stretch if children are small, so we do our own
				tabs = { Tabs() }
			)
		} else {
			PrimaryTabRow(
				modifier = Modifier.verticalScroll(rememberScrollState()),
				selectedTabIndex = pagerState.currentPage,
				containerColor = Color.Transparent,
				divider = {}, // It doesn't stretch if children are small, so we do our own
				tabs = { Tabs() }
			)
		}

		HorizontalDivider()
	}

	HorizontalPager(
		modifier = Modifier.weight(1f),
		state = pagerState
	) { page ->
		when(tabs[page]) {
			MediaScreenTabs.INFO -> Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(contentPadding)
					.padding(horizontal = 16.dp)
					.padding(top = if(tabs.size > 1) 20.dp else 8.dp, bottom = 16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				media.description?.also { description ->
					if(description.isBlank()) return@also
					var isExpanded by remember { mutableStateOf(false) }

					SelectionContainer {
						ExpandableText(
							isExpanded = isExpanded,
							onExpand = { isExpanded = it },
							maxLines = 5,
							text = remember(description) {
								htmlToAnnotatedString(
									html = description.trim(),
									compactMode = true
								)
							}
						)
					}
				}

				media.tags?.also { tags ->
					if(tags.isEmpty()) return@also

					Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
						Text(
							style = MaterialTheme.typography.titleLarge,
							text = "Tags"
						)

						FlowRow(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							tags.forEach { tag ->
								SuggestionChip(
									onClick = {},
									label = { Text(tag) }
								)
							}
						}
					}
				}
			}

			MediaScreenTabs.EPISODES -> {
				val currentRoute = rememberSaveable(
					saver = stateListSaver()
				) { mutableStateListOf<String>() }
				
				if(currentRoute.isNotEmpty()) {
					BackEffect { 
						currentRoute.removeLastOrNull()
					}
				}
				
				val currentRoutes by remember { 
					derivedStateOf {
						buildList {
							var currentNode: WatcherNode = watcher
							add(currentNode)
							
							currentRoute.iterateMutable { path ->
								if(currentNode is WatcherNode.Variants) {
									currentNode.children.firstOrNull { it.id == path }?.also { selected ->
										add(selected)
										currentNode = selected
									} ?: run {
										Log.w("DefaultMediaScreen", "Path node \"$path\" cannot be found in " +
												"\"${currentNode.id}\", so we strip the path.")
										
										removeAll()
										return@iterateMutable
									}
								} else {
									Log.w("DefaultMediaScreen", 
										"Path node \"${currentNode.id}\" is not a list, " +
												"so we cannot navigate to \"$path\".")
									
									removeAll()
									return@iterateMutable
								}
							}
						}
					}
				}
				
				Column {
					val scrollState = rememberScrollState()
					val areExtensionsLoading by Extensions.observeIsLoading().collectAsState()
					
					val extensions by Extensions.observeAll(enabled = true).map { extensions -> 
						extensions.filter { extension ->
							extension.cachedModules.any { it is WatchModule }
						}
					}.collectAsState(emptyList())
					
					if(!areExtensionsLoading && extensions.isEmpty()) {
						InfoBox(
							modifier = Modifier
								.fillMaxSize()
								.verticalScroll(rememberScrollState()),
							title = "No extensions",
							message = "You don't have any extensions installed that can provide videos! " +
									"Playback isn't available."
						)
						
						return@Column
					}
					
					Breadcrumb(
						modifier = Modifier
							.fillMaxWidth()
							.verticalScroll(rememberScrollState()),
						contentPadding = contentPadding.exclude(bottom = true),
						scrollState = scrollState
					) {
						currentRoutes.iterateIndexed { index, node ->
							item(
								title = {
									Text(
										modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
										text = node.title
									)
								},
								
								onClick = {
									if(currentRoute.isNotEmpty()) {
										currentRoute.removeRange(index, currentRoute.size)
									}
								}
							)
							
							if(hasNext()) {
								separator()
							}
						}
					}
					
					val pagerState = rememberPagerState { currentRoutes.size }
					
					HorizontalPager(
						modifier = Modifier.fillMaxSize(),
						state = pagerState,
						userScrollEnabled = false
					) { page ->
						val route = currentRoutes[page] as WatcherNode.Variants
						
						LazyColumn(
							modifier = Modifier.fillMaxSize(),
							contentPadding = contentPadding
								.exclude(top = true)
								.add(bottom = 16.dp)
						) {
							/**
							 * @return Ratio from 0 to 100
							 */
							fun WatcherNode.getSamenessRatio(): Int {
								return listOf(media.title, *media.alternativeTitles.toTypedArray())
									.map { FuzzySearch.ratio(title, media.title) }
									.maxBy { it }
							}
							
							@Composable
							fun LazyItemScope.Item(node: WatcherNode) {
								Surface(
									modifier = Modifier
										.fillMaxWidth()
										.animateItem(),
									color = Color.Transparent,
									onClick = {
										val wasSize = pagerState.pageCount
										
										fun onClick(node: WatcherNode) {
											if(node is WatcherNode.Video) {
												navigation.push(Routes.Player(
													video = node.video,
													title = currentRoutes.mapNotNull {
														it as? VariantWatcher
													}.lastOrNull {
														it.variant.type == WatchVariant.Type.EPISODE
													}?.variant?.title ?: node.video.let {
														it.title ?: it.url
													}
												))
												
												return
											}
											
											currentRoute += node.id

											if(node is WatcherNode.Variants && !node.isLoading && node.children.size == 1) {
												val firstNode = node.children.firstOrNull() ?: return
												currentRoute += firstNode.id
												onClick(firstNode)
												return
											}

											(node as? WatcherNode.Variants)?.children?.map { 
												it to it.getSamenessRatio()
											}?.sortedByDescending { 
												it.second
											}?.firstOrNull {
												// Just like in Dantotsu
												it.second > 80 
											}?.also { sameNode ->
												currentRoute += sameNode.first.id
												onClick(sameNode.first)
											}
										}
										
										onClick(node)
										
										if(pagerState.pageCount > wasSize) {
											coroutineScope.launch(Dispatchers.IO) {
												await { pagerState.canScrollForward }
												
												withContext(Dispatchers.Main) {
													pagerState.animateScrollToPage(currentRoute.size + 1)
													scrollState.animateScrollTo(Int.MAX_VALUE)
												}
											}
										}
									}
								) {
									Row(
										modifier = Modifier
											.fillMaxWidth()
											.heightIn(min = 48.dp)
											.padding(horizontal = 16.dp, vertical = 8.dp),
										verticalAlignment = Alignment.CenterVertically,
										horizontalArrangement = Arrangement.spacedBy(16.dp)
									) {
										Text(
											modifier = Modifier
												.weight(1f)
												.animateItem(),
											text = node.title
										)
										
										when {
											node.isLoading -> {
												CircularProgressIndicator(
													modifier = Modifier.size(24.dp)
												)
											}
											
											node.error != null -> {
												Icon(
													modifier = Modifier.size(24.dp),
													painter = painterResource(Res.drawable.ic_block),
													contentDescription = null,
													tint = MaterialTheme.colorScheme.error
												)
											}
											
											node is WatcherNode.Variants -> {
												Text(
													color = MaterialTheme.colorScheme.secondary,
													text = node.children.size.toString()
												)
											}
										}
									}
								}
							}
							
							singleItem("scrollFixer") {
								Spacer(Modifier.height(1.dp))
							}
							
							items(
								key = { it.id },
								items = retryUntilSuccess { 
									route.children
										.filter { it.error == null && !it.isLoading }
										.sortedByDescending { watcher ->
											(watcher as? VariantWatcher)?.variant?.number?.also { 
												return@sortedByDescending it
											}
										
											watcher.getSamenessRatio().toFloat()
										}
								}
							) { Item(it) }
							
							if(route.isLoading || retryUntilSuccess { 
								route.children.count { it.isLoading } != 0
							}) {
								singleItem("loading") {
									LoadingIndicator(
										modifier = Modifier
											.fillMaxWidth()
											.padding(if(route.children.isEmpty()) 64.dp else 32.dp)
											.wrapContentSize(Alignment.Center)
											.animateItem()
									)
								}
							}

							items(
								key = { it.id },
								items = retryUntilSuccess {
									route.children
										.filter { it.error != null }
										.sortedBy { it.title }
								}
							) { Item(it) }
							
							route.error?.also { error ->
								singleItem("error") {
									InfoBox(
										modifier = Modifier
											.fillMaxWidth()
											.wrapContentSize(Alignment.Center)
											.animateItem(),
										contentPadding = PaddingValues(64.dp),
										throwable = error,
										actions = {
											action("Try again") {
												coroutineScope.launch(Dispatchers.Default) {
													route.load()
												}
											}
										}
									)
								}
							}
						}
					}
					
//					LazyVerticalGrid(
//						modifier = Modifier.fillMaxSize(),
//						columns = GridCells.Adaptive(64.dp),
//						contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//						horizontalArrangement = Arrangement.spacedBy(8.dp),
//						verticalArrangement = Arrangement.spacedBy(2.dp)
//					) {
//						items(
//							count = 1001,
//							key = { it },
//							contentType = { "episode" }
//						) {
//							Card(
//								onClick = {}
//							) {
//								Text(
//									modifier = Modifier
//										.padding(8.dp)
//										.align(Alignment.CenterHorizontally),
//									fontFamily = AweryFonts.poppins,
//									fontWeight = FontWeight.SemiBold,
//									text = "$it"
//								)
//							}
//						}
//					}
				}
			}

			else -> Text(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState()),
				textAlign = TextAlign.Center,
				text = "This page isn't done yet!"
			)
		}
	}
}