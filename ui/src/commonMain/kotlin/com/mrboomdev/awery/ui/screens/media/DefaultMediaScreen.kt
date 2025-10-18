package com.mrboomdev.awery.ui.screens.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.kmpalette.loader.rememberNetworkLoader
import com.kmpalette.rememberDominantColorState
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.await
import com.mrboomdev.awery.core.utils.collection.iterateIndexed
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.core.utils.retryUntilSuccess
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.cachedModules
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.loaders.getBanner
import com.mrboomdev.awery.extension.loaders.getLargePoster
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.loaders.watch.VariantWatcher
import com.mrboomdev.awery.extension.loaders.watch.WatcherNode
import com.mrboomdev.awery.extension.sdk.*
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.extension.sdk.modules.WatchModule
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.ic_block
import com.mrboomdev.awery.resources.ic_share_filled
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.*
import com.mrboomdev.awery.ui.effects.BackEffect
import com.mrboomdev.awery.ui.screens.GalleryScreen
import com.mrboomdev.awery.ui.theme.SeedAweryTheme
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.navigation.core.safePop
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DefaultMediaScreen(
	destination: Routes.Media,
	viewModel: MediaScreenViewModel,
	contentPadding: PaddingValues
) {
	val topBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	val infoHeaderBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
		snapAnimationSpec = null)
	val coroutineScope = rememberCoroutineScope()
	val media by viewModel.media.collectAsState()
	val windowSize = currentWindowSize()
	val toaster = LocalToaster.current
	val navigation = Navigation.current()

	val tabs = remember(media) {
		MediaScreenTabs.getVisibleFor(media)
	}

	var showGallery by rememberSaveable { mutableStateOf(false) }
	val pagerState = rememberPagerState { tabs.count() }

	val defaultColor = remember(media) {
		if(media.getLargePoster() == null) return@remember null

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

	LaunchedEffect(media) {
		media.getLargePoster()?.also { poster ->
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
							var didFailToLoadPoster by remember(media.getPoster()) {
								mutableStateOf(media.getLargePoster() == null)
							}

							var didLoadPoster by remember(media.getPoster()) { 
								mutableStateOf(false) 
							}
							
							var isPosterFuckedUp by remember(media.getPoster()) {
								mutableStateOf(false) 
							}

							media.getBanner()?.also { banner ->
								Box(Modifier.matchParentSize()) {
									val alpha by animateFloatAsState(if(isPosterFuckedUp) {
										.5f
									} else .25f)

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
									.padding(contentPadding.only(top = true, start = true, end = true))
							) {
								Spacer(Modifier.height(animateDpAsState(when {
									didLoadPoster && isPosterFuckedUp -> 48.dp
									didFailToLoadPoster -> 56.dp
									else -> 16.dp
								}, tween()).value))
								
								media.getLargePoster()?.also { poster ->
									val context = LocalPlatformContext.current
									
									val model = remember(poster) {
										poster.let {
											ImageRequest.Builder(context)
												.placeholderMemoryCacheKey(it)
												.memoryCacheKey(it)
												.data(it)
												.build()
										}
									}
									
									if(isPosterFuckedUp) {
										AsyncImage(
											modifier = Modifier
												.padding(top = 8.dp, bottom = 16.dp, horizontal = 8.dp)
												.clip(RoundedCornerShape(16.dp))
												.fillMaxWidth()
												.clickable { showGallery = true }
												.animateContentSize(),
											
											model = model,
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
											.clickable { showGallery = true }
											.animateContentSize(),

										model = model,
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
											isPosterFuckedUp = result.image.let {
												it.width / it.height
											} >= 1
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
							media.url?.also { url ->
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
			) { scaffoldContentPadding ->
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(scaffoldContentPadding.exclude(bottom = true))
				) {
					MediaScreenContent(
						media = media,
						extensionId = destination.extensionId,
						watcher = viewModel.watcher.collectAsState().value,
						pagerState = pagerState,
						tabs = tabs,
						coroutineScope = coroutineScope,
						contentPadding = scaffoldContentPadding.only(bottom = true)
							.plus(contentPadding.only(bottom = true))
					)
				}
			}
		}

		@Composable
		fun Landscape() {
			Box(Modifier.fillMaxSize()) {
				media.getBanner().also { banner ->
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
							.padding(contentPadding.only(start = true, top = true, bottom = true)),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						TopButton(
							painter = painterResource(Res.drawable.ic_back),
							onClick = { navigation.safePop() }
						)

						media.url?.also { url ->
							TopButton(
								padding = 9.dp,
								painter = painterResource(Res.drawable.ic_share_filled),
								onClick = { Awery.share(url) }
							)
						}
					}

					var showPoster by remember(media) { mutableStateOf(true) }
					media.getLargePoster()?.also { poster ->
						if(!showPoster || windowSize.width <= WindowSizeType.Medium) return@also

						Box(
							modifier = Modifier
								.padding(top = 8.dp, bottom = 16.dp)
								.padding(contentPadding.only(vertical = true))
						) {
							AsyncImage(
								modifier = Modifier
									.clip(RoundedCornerShape(16.dp))
									.fillMaxHeight()
									.animateContentSize()
									.clickable { showGallery = true },

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
										.padding(horizontal = 16.dp)
										.padding(top = 16.dp)
										.padding(contentPadding.only(top = true, end = true))
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
					) { scaffoldContentPadding ->
						Column(
							modifier = Modifier
								.padding(scaffoldContentPadding.exclude(
									start = true, end = true, bottom = true))
								.fillMaxSize()
						) {
							Spacer(Modifier.height(animateDpAsState(
								if(infoHeaderBehavior.state.let { 
									it.heightOffset <= it.heightOffsetLimit 
								}) contentPadding.top else 8.dp,
								animationSpec = tween(500)
							).value))

							MediaScreenContent(
								media = media,
								extensionId = destination.extensionId,
								watcher = viewModel.watcher.collectAsState().value,
								pagerState = pagerState,
								tabs = tabs,
								coroutineScope = coroutineScope,
								contentPadding = scaffoldContentPadding.only(end = true, bottom = true)
							)
						}
					}
				}
			}
		}

		Box(Modifier.fillMaxSize()) {
			if(windowSize.width >= WindowSizeType.Large 
				|| windowSize.height <= WindowSizeType.Small
			) Landscape() else Portrait()
			
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
	contentPadding: PaddingValues,
	extensionId: String
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
				onClick = { coroutineScope.launch {
					pagerState.animateScrollToPage(index)
				} }
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
								var isLoading by remember { mutableStateOf(false) }
								val coroutineScope = rememberCoroutineScope()
								val toaster = LocalToaster.current
								
								if(isLoading) {
									Dialog(onDismissRequest = {}) {
										CircularProgressIndicator()
									}
								}
								
								SuggestionChip(
									onClick = {
										isLoading = true
										
										coroutineScope.launchTrying(Dispatchers.Default, onCatch = {
											toaster.toast("Failed to search by a tag")
											isLoading = false
										}) { 
											Extensions[extensionId]?.also scope@{ extension ->
												extension.get<CatalogModule>()?.also { catalogModule ->
													val filters = catalogModule.getDefaultFilters()
													
													fun List<Preference<*>>.findPref(
														predicate: (Preference<*>) -> Boolean
													): Preference<*>? = firstOrNull { preference ->
														if(predicate(preference)) {
															return@firstOrNull true
														}
														
														if(preference is PreferenceGroup) {
															preference.items.findPref(predicate)?.also { child ->
																return child
															}
														}
														
														false
													}

													filters.findPref {
														it.name.equals(tag, ignoreCase = true)
													}?.also { 
														when(it) {
															is BooleanPreference -> {
																it.value = true
															}

															is TriStatePreference -> {
																it.value = TriStatePreference.State.INCLUDED
															}
															
															is IntPreference, 
															is LongPreference,
															is SelectPreference,
															is LabelPreference,
															is PreferenceGroup,
															is StringPreference -> {
																toaster.toast(
																	title = "Failed to search",
																	message = "Cannot apply an filter of type ${it::class.qualifiedName}",
																	duration = 5_000
																)

																return@scope	
															}
														}
													} ?: filters.findPref { 
														it.role == Preference.Role.QUERY
													}?.also { 
														if(it is StringPreference) {
															it.value = tag
														} else {
															toaster.toast(
																title = "Failed to search",
																message = "Invalid extension filter type! Contact extension developer so that he can fix it.",
																duration = 5_000
															)
															
															return@scope
														}
													} ?: run {
														toaster.toast(
															title = "Unable to perform search",
															message = "Source extension doesn't support search.",
															duration = 5_000
														)

														return@scope
													}
													
													launch(Dispatchers.Main) {
														navigation.push(Routes.ExtensionSearch(
															extensionId = extensionId,
															extensionName = extension.name,
															filters = filters
														))
													}
												} ?: run {
													toaster.toast(
														title = "Search isn't supported!",
														message = "Source extension doesn't support search.",
														duration = 5_000
													)
												}
											} ?: run {
												toaster.toast(
													title = "Extension isn't installed!",
													message = "It is missing and is required to search by an tag. Install it to perform the search.",
													duration = 5_000
												)
											}
											
											isLoading = false
										}
									},
									
									label = { Text(tag) }
								)
							}
						}
					}
				}
			}

			MediaScreenTabs.EPISODES -> {
				val path = rememberSaveable(watcher, saver = listSaver(
					save = { mutableStateList ->
						mutableStateList.map { node ->
							node.id
						}
					},
					
					restore = { savedIds ->
						mutableStateListOf<WatcherNode.Variants>().apply {
							var currentNode = watcher as WatcherNode.Variants
							
							savedIds.forEachIndexed { index, id ->
								if(index == 0) return@forEachIndexed
								
								currentNode = currentNode.children.find {
									it.id == id
								} as? WatcherNode.Variants ?: return@apply
								
								add(currentNode)
							}
						}
					}
				)) { mutableStateListOf(watcher as WatcherNode.Variants) }
				
				if(path.size > 1) {
					BackEffect {
						path.removeLastOrNull()
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
						path.iterateIndexed { index, node ->
							item(
								title = {
									Text(
										modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
										text = node.title
									)
								},
								
								onClick = {
									path.removeRange(index + 1, path.size)
								}
							)
							
							if(hasNext()) {
								separator()
							}
						}
					}
					
					val pagerState = rememberPagerState { path.size }
					
					HorizontalPager(
						modifier = Modifier.fillMaxSize(),
						state = pagerState,
						userScrollEnabled = false
					) { page ->
						val route = path[page]
						
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
											when(val node = node) {
												is WatcherNode.Variants -> {
													path += node
													
													if(node.isLoading || node.children.isEmpty()) return
													
													if(node.children.size == 1) {
														onClick(node.children[0])
														return
													}

													node.children.maxBy {
														it.getSamenessRatio()
													}.also { child ->
														if(child.getSamenessRatio() > 80) {
															// Just like in Dantotsu
															onClick(child)
														}
													}
												}
												
												is WatcherNode.Video -> {
													navigation.push(Routes.Player(
														video = node.video,
														title = path.mapNotNull {
															it as? VariantWatcher
														}.lastOrNull {
															it.variant.type == WatchVariant.Type.EPISODE
														}?.variant?.title ?: node.video.let {
															it.title ?: it.url
														}
													))
												}
											}
										}
										
										onClick(node)

										if(pagerState.pageCount > wasSize) {
											coroutineScope.launch(Dispatchers.IO) {
												await { pagerState.canScrollForward }

												withContext(Dispatchers.Main) {
													pagerState.animateScrollToPage(Int.MAX_VALUE)
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