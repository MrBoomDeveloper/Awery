package com.mrboomdev.awery.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.extension.loaders.ExtensionInstaller
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.components.*
import com.mrboomdev.awery.ui.effects.InsetsController
import com.mrboomdev.awery.ui.navigation.*
import com.mrboomdev.awery.ui.screens.intro.steps.IntroThemeStep
import com.mrboomdev.awery.ui.screens.intro.steps.IntroUserStep
import com.mrboomdev.awery.ui.screens.intro.steps.IntroWelcomeStep
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.navigation.core.Navigation
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.bringToTop
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

interface App {
    fun reloadWallpaper()
	
	companion object {
		val searchQuery = MutableStateFlow("")
	}
}

val LocalApp = staticCompositionLocalOf<App> { 
    throw IllegalStateException("LocalApp isn't initialized!")
}

@Composable
private fun showNavLabel(isActive: Boolean): Boolean {
	return when(AwerySettings.showNavigationLabels.collectAsState().value) {
		AwerySettings.NavigationLabels.SHOW -> true
		AwerySettings.NavigationLabels.HIDE -> false
		AwerySettings.NavigationLabels.ACTIVE -> isActive
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(onNavigate: (NavigationState) -> Unit = {}) {
    AweryTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
			val context = LocalPlatformContext.current
			val backgroundColor = MaterialTheme.colorScheme.background
			val wallpaperOpacity = AwerySettings.wallpaperOpacity.collectAsState().value / 100f
			val windowSize = currentWindowSize()

			val topAppBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
			val tabContentOffsets = rememberSaveable { mutableMapOf<Int, Float>() }
			val coroutineScope = rememberCoroutineScope()
			val drawerState = rememberDrawerState(DrawerValue.Closed)
			val toaster = remember { Toaster(maxItems = 3) }
			val navigationMap = rememberNavigationMap()
			
			val pagerState = rememberPagerState(
				initialPage = when(AwerySettings.mainDefaultTab.value) {
					AwerySettings.MainTab.HOME -> MainRoutes.HOME
					AwerySettings.MainTab.SEARCH -> MainRoutes.SEARCH
					AwerySettings.MainTab.NOTIFICATIONS -> MainRoutes.NOTIFICATIONS
					AwerySettings.MainTab.LIBRARY -> MainRoutes.LIBRARY
				}.ordinal
			) { MainRoutes.entries.size }
			
			val currentNavigation = navigationMap[pagerState.currentPage]
			val currentRoute by currentNavigation.currentDestinationFlow.collectAsState(null)
			
			val routeInfos = remember { 
				mutableStateListOf(
					RouteInfo(
						route = Routes.Home,
						title = null,
						displayHeader = true,
						displayNavigationBar = true,
						fullscreen = false
					)
				)
			}
			
			val currentRouteInfo = routeInfos.lastOrNull { routeInfo ->
				routeInfo.route == currentRoute
			} ?: routeInfos.last()

			InsetsController(
				hideBars = currentRouteInfo.fullscreen
			)
			
			val wallpaperPainter = rememberAsyncImagePainter(
                filterQuality = FilterQuality.High,
                model = remember {
                    ImageRequest.Builder(context)
                        .addLastModifiedToFileCacheKey(true)
                        .data(FileKit.filesDir / "wallpaper.png")
                        .build()
                },
                
                contentScale = ContentScale.Crop
            )
			
			fun onOpenTab(index: Int, route: Routes) {
				tabContentOffsets[pagerState.currentPage] = topAppBarBehavior.state.contentOffset
				topAppBarBehavior.state.contentOffset = tabContentOffsets[index] ?: 0f

				if(index == pagerState.currentPage) {
					navigationMap[index].bringToTop(route)
					onNavigate(NavigationState(route, null))
				} else {
					coroutineScope.launch { 
						pagerState.animateScrollToPage(index)
						
						onNavigate(
							NavigationState(
							route = route,
							goBack = navigationMap[index].let { navigation ->
								if (navigation.canPop) {{
									navigation.safePop()
								}} else null
							}
						))
					}
				}
			}

			LaunchedEffect(currentNavigation) {
				currentNavigation.currentBackStackFlow.collect { backStack ->
					onNavigate(
						NavigationState(
							route = backStack.lastOrNull() ?: Routes.Home,
							goBack = if (backStack.size > 1) {{
								currentNavigation.safePop()
							}} else null
						)
					)
				}
			}

			RememberLaunchedEffect(Unit) {
				when {
					!AwerySettings.introDidWelcome.value -> Routes.Intro(IntroWelcomeStep, singleStep = false)
					!AwerySettings.introDidTheme.value -> Routes.Intro(IntroThemeStep, singleStep = false)
					AwerySettings.username.value.isBlank() -> Routes.Intro(IntroUserStep, singleStep = false)
					else -> null
				}.also {
					if(it == null) {
						onNavigate(NavigationState(currentNavigation.currentDestination, null))
						return@also
					}

					coroutineScope.launch(Dispatchers.Main) {
						currentNavigation.apply {
							clear()
							push(it)
							onNavigate(NavigationState(it, null))
						}
					}
				}
			}

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val gradient = Brush.verticalGradient(listOf(
                            Color.Transparent,
                            backgroundColor.copy(alpha = (1f - wallpaperOpacity) * 2f)
                        ))

                        onDrawWithContent {
                            drawContent()
                            drawRect(gradient)
                        }
                    },

                painter = wallpaperPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = wallpaperOpacity
            )

            CompositionLocalProvider(
                LocalToaster provides toaster,
				LocalLayoutDirection provides LayoutDirection.Rtl,
				
                LocalApp provides remember { 
                    object : App {
                        override fun reloadWallpaper() {
                            wallpaperPainter.restart()
                        }
                    }
                },

				LocalRouteInfoCollector provides remember {
					object : RouteInfoCollector {
						
						override fun add(routeInfo: RouteInfo) {
							routeInfos += routeInfo
						}

						override fun remove(routeInfo: RouteInfo) {
							routeInfos -= routeInfo
						}
					}
				}
            ) {
				ModalNavigationDrawer(
					modifier = Modifier.fillMaxSize(),
					drawerState = drawerState,
					gesturesEnabled = currentRouteInfo.displayHeader && Awery.platform != Platform.DESKTOP,
					drawerContent = { AweryDrawerContent(currentNavigation, drawerState, coroutineScope) }
				) {
					CompositionLocalProvider(
						LocalLayoutDirection provides LayoutDirection.Ltr
					) {
						Row {
							val useRail = windowSize.width >= WindowSizeType.Large

							AnimatedVisibility(useRail && currentRouteInfo.displayNavigationBar) {
								AwerySideBar(
									translucent = when(currentRoute) {
										is Routes.Media -> false
										else -> true 
									},
										
									currentTab = pagerState.currentPage,
									onOpenTab = ::onOpenTab
								)
							}

							Column {
								val installing by ExtensionInstaller.observeInstalling().collectAsState()
								val hazeState = rememberHazeState()

								AnimatedVisibility(installing.isNotEmpty()) {
									Row(
										modifier = Modifier
											.background(MaterialTheme.colorScheme.primaryContainer)
											.windowInsetsPadding(WindowInsets.safeDrawing.only(
												WindowInsetsSides.Top + if(useRail) {
													WindowInsetsSides.Right
												} else WindowInsetsSides.Horizontal
											)).padding(16.dp),
										horizontalArrangement = Arrangement.spacedBy(24.dp),
										verticalAlignment = Alignment.CenterVertically
									) {
										CircularProgressIndicator(
											modifier = Modifier.size(16.dp),
											color = MaterialTheme.colorScheme.onPrimaryContainer
										)

										Text(
											modifier = Modifier.weight(1f),
											color = MaterialTheme.colorScheme.onPrimaryContainer,
											fontWeight = FontWeight.SemiBold,
											text = "Installing ${installing.size} extensions"
										)
									}
								}

								Scaffold(
									modifier = Modifier
										.fillMaxSize()
										.nestedScroll(topAppBarBehavior.nestedScrollConnection),
										
									containerColor = Color.Transparent,
									contentWindowInsets = WindowInsets.safeDrawing.only(
										WindowInsetsSides.Vertical + when(useRail) {
											true -> WindowInsetsSides.Right
											false -> WindowInsetsSides.Horizontal
										}
									),

									topBar = {
										AnimatedVisibility(currentRouteInfo.displayHeader && Awery.platform != Platform.DESKTOP) {
											AweryTopBar(
												topAppBarBehavior = topAppBarBehavior,
												showSearch = currentRoute == Routes.Search,
												onOpenDrawer = { drawerState.open() },
												currentRouteInfo = currentRouteInfo,
												navigation = currentNavigation
											)
										}
									},

									bottomBar = {
										AnimatedVisibility(!useRail && currentRouteInfo.displayNavigationBar) {
											Box(Modifier.hazeEffect(state = hazeState, style = HazeDefaults.style(
												blurRadius = 4.dp,
												backgroundColor = MaterialTheme.colorScheme.surface
											))) {
												AweryBottomBar(
													currentTab = pagerState.currentPage,
													onOpenTab = ::onOpenTab
												)
											}
										}
									}
								) { contentPadding ->
									if(useRail)  {
										VerticalPager(
											modifier = Modifier
												.fillMaxSize()
												.hazeSource(state = hazeState),
											userScrollEnabled = false,
											state = pagerState
										) { index ->
											AweryNavHost(
												navigation = navigationMap[index],
												contentPadding = contentPadding
											)
										}
									} else {
										HorizontalPager(
											modifier = Modifier
												.fillMaxSize()
												.hazeSource(state = hazeState),
											userScrollEnabled = false,
											state = pagerState
										) { index ->
											AweryNavHost(
												navigation = navigationMap[index],
												contentPadding = contentPadding
											)
										}
									}
								}
							}
						}
					}
				}
            }

            ToasterContainer(
                state = toaster,
                contentAlignment = if(windowSize.width >= WindowSizeType.Large) {
                    Alignment.BottomStart
                } else Alignment.BottomCenter
            )
        }
    }
}

@Composable
private fun AweryDrawerContent(
	navigation: Navigation<Routes>,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope
) {
	ModalDrawerSheet(
		drawerState = drawerState,
		windowInsets = WindowInsets.none
	) {
		CompositionLocalProvider(
			LocalLayoutDirection provides LayoutDirection.Ltr
		) {
			Column(
				modifier = Modifier
					.fillMaxHeight()
					.width(275.dp)
					.verticalScroll(rememberScrollState())
					.windowInsetsPadding(WindowInsets.safeDrawing.only(
						WindowInsetsSides.Bottom
					)).padding(bottom = 12.dp)
			) {
				Box(Modifier.fillMaxWidth()) {
					AsyncImage(
						modifier = Modifier
							.matchParentSize(),

						model = ImageRequest.Builder(LocalPlatformContext.current)
							.addLastModifiedToFileCacheKey(true)
							.data(FileKit.filesDir / "wallpaper.png")
							.build(),

						alpha = .5f,
						contentDescription = null,
						contentScale = ContentScale.Crop
					)

					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 24.dp, vertical = 12.dp)
							.windowInsetsPadding(WindowInsets.safeDrawing.only(
								WindowInsetsSides.Top + WindowInsetsSides.End
							))
					) {
						Column(
							modifier = Modifier.weight(1f),
							verticalArrangement = Arrangement.spacedBy(10.dp)
						) {
							Surface(
								modifier = Modifier.size(56.dp),
								shape = CircleShape,
								color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
								contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
								onClick = {
									coroutineScope.launch {
										drawerState.open()
									}
								}
							) {
								Icon(
									modifier = Modifier
										.fillMaxSize()
										.padding(6.dp),

									painter = painterResource(Res.drawable.ic_account_outlined),
									contentDescription = null
								)

								AsyncImage(
									modifier = Modifier.fillMaxSize(),

									model = ImageRequest.Builder(LocalPlatformContext.current)
										.addLastModifiedToFileCacheKey(true)
										.data(FileKit.filesDir / "avatar.png")
										.build(),

									contentDescription = null,
									contentScale = ContentScale.Crop
								)
							}

							Text(
								style = MaterialTheme.typography.titleMedium,
								color = MaterialTheme.colorScheme.onBackground,
								text = AwerySettings.username.collectAsState().value
							)
						}

						IconButton(
							modifier = Modifier
								.offset(9.dp, (-9).dp)
								.size(40.dp),
							padding = 9.dp,
							painter = painterResource(Res.drawable.ic_edit_outlined),
							contentDescription = null,
							onClick = {
								navigation.push(
									Routes.Intro(
									step = IntroUserStep,
									singleStep = true
								))
								
								coroutineScope.launch { 
									drawerState.close()
								}
							}
						)
					}
				}

				HorizontalDivider(
					modifier = Modifier
						.padding(end = WindowInsets.safeDrawing.right)
						.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
				)

				SettingsDefaults.itemClickable(
					contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
					icon = painterResource(Res.drawable.ic_bookmarks_outlined),
					title = "Lists",
					onClick = {
						coroutineScope.launch { drawerState.close() }
						navigation.push(Routes.Settings(SettingsPages.Lists))
					}
				)
				
				if(AwerySettings.mediaHistory.collectAsState().value) {
					SettingsDefaults.itemClickable(
						contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
						icon = painterResource(Res.drawable.ic_history),
						title = "History",
						onClick = {
							navigation.push(Routes.History)
							
							coroutineScope.launch { 
								drawerState.close()
							}
						}
					)
				}

				SettingsDefaults.itemClickable(
					contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
					icon = painterResource(Res.drawable.ic_extension_outlined),
					title = stringResource(Res.string.extensions),
					onClick = {
						coroutineScope.launch { drawerState.close() }
						navigation.push(Routes.Settings(SettingsPages.Extensions))
					}
				)

				//                            Spacer(Modifier.weight(1f))

				SettingsDefaults.itemClickable(
					contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
					icon = painterResource(Res.drawable.ic_settings_outlined),
					title = stringResource(Res.string.settings),
					onClick = {
						coroutineScope.launch { drawerState.close() }
						navigation.push(Routes.Settings())
					}
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AweryTopBar(
	topAppBarBehavior: TopAppBarScrollBehavior,
	showSearch: Boolean,
	onOpenDrawer: suspend () -> Unit,
	currentRouteInfo: RouteInfo?,
	navigation: Navigation<*>
) {
	val coroutineScope = rememberCoroutineScope()
	val windowSize = currentWindowSize()
	
	FlexibleTopAppBar(
		modifier = Modifier.fillMaxWidth(),
		scrollBehavior = topAppBarBehavior,
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = Color.Transparent,
			scrolledContainerColor = MaterialTheme.colorScheme.background.let {
				if(isAmoledTheme()) it.copy(alpha = .9f) else it
			}
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.windowInsetsPadding(WindowInsets.safeDrawing.only(
					WindowInsetsSides.Top + WindowInsetsSides.Right
				)).padding(horizontal = niceSideInset(), vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			val canPop by navigation.currentBackStackFlow.map { backStack ->
				backStack.size > 1
			}.collectAsState(navigation.currentBackStack.size > 1)
			
			Crossfade(canPop) { showBackButton ->
				if(showBackButton) {
					IconButton(
						modifier = Modifier.size(36.dp),
						padding = 3.dp,
						painter = painterResource(Res.drawable.ic_back),
						contentDescription = null,
						onClick = {
                            navigation.safePop()
						}
					)
					
					return@Crossfade
				}
				
				Image(
					modifier = Modifier.size(36.dp),
					painter = painterResource(Res.drawable.logo_awery),
					contentDescription = null
				)
			}

			AnimatedVisibility(
				visible = !showSearch || windowSize.width >= WindowSizeType.Large
			) {
				Crossfade(currentRouteInfo?.title ?: "Awery") { title ->
					Text(
						style = MaterialTheme.typography.titleLarge,
						text = title
					)
				}
			}

			Box(Modifier.weight(1f)) {
				val spacing = when {
					windowSize.width >= WindowSizeType.Large -> 32.dp
					windowSize.width >= WindowSizeType.Medium -> 16.dp
					else -> 8.dp
				}

				this@Row.AnimatedVisibility(
					visible = showSearch,
					enter = fadeIn(),
					exit = fadeOut()
				) {
					Row(
						modifier = Modifier
							.padding(horizontal = spacing)
							.clip(RoundedCornerShape(48.dp))
							.background(MaterialTheme.colorScheme.surfaceContainerHighest.let {
								if(AwerySettings.amoledTheme.collectAsState().value) it.copy(alpha = .75f) else it
							})
							.border(.5.dp, Color(0x22ffffff), RoundedCornerShape(48.dp))
							.widthIn(max = 400.dp)
							.fillMaxWidth()
							.height(48.dp)
					) {
						val focusRequester = remember { FocusRequester() }
						val query by App.searchQuery.collectAsState()

						BasicTextField(
							modifier = Modifier
								.fillMaxSize()
								.padding(horizontal = 20.dp)
								.focusRequester(focusRequester),
							value = query,
							singleLine = true,
							cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),

							onValueChange = {
								coroutineScope.launch {
									App.searchQuery.emit(it)
								}
							},

							textStyle = MaterialTheme.typography.bodyMedium.copy(
								color = MaterialTheme.colorScheme.onBackground
							),

							keyboardOptions = KeyboardOptions(
								imeAction = ImeAction.Search
							),

							decorationBox = {
								Box(
									modifier = Modifier
										.fillMaxSize()
										.wrapContentWidth(Alignment.Start),
									contentAlignment = Alignment.Center
								) {
									if(query.isEmpty()) {
										Text(
											style = MaterialTheme.typography.bodyMedium,
											color = MaterialTheme.colorScheme.onSurfaceVariant,
											text = "Search"
										)
									}
								}

								Row(
									modifier = Modifier.fillMaxSize(),
									verticalAlignment = Alignment.CenterVertically
								) {
									Box(Modifier.weight(1f)) {
										it()
									}

									if(query.isNotEmpty()) {
										CompositionLocalProvider(
											LocalContentColor provides Color.White
										) {
											IconButton(
												modifier = Modifier
													.heightIn(max = 40.dp)
													.fillMaxHeight()
													.aspectRatio(1f)
													.offset(x = 10.dp),

												painter = painterResource(Res.drawable.ic_close),
												contentDescription = null,

												colors = IconButtonDefaults.iconButtonColors(
													contentColor = MaterialTheme.colorScheme.onSurfaceVariant
												),

												onClick = {
													coroutineScope.launch {
														focusRequester.requestFocus()
														App.searchQuery.emit("")
													}
												}
											)
										}
									}
								}
							}
						)
					}
				}
			}

			Row(
				modifier = Modifier
					.clip(RoundedCornerShape(32.dp))
					.clickable {
						coroutineScope.launch {
							onOpenDrawer()
						}
					}.thenIf(windowSize.width >= WindowSizeType.Large) {
						padding(start = 16.dp)
					},

				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				if(windowSize.width >= WindowSizeType.Large) {
					Text(
						modifier = Modifier.padding(end = 2.dp),
						style = MaterialTheme.typography.bodyLarge,
						fontFamily = AweryFonts.poppins,
						fontWeight = FontWeight.Normal,
						text = AwerySettings.username.collectAsState().value
					)
				}

				FilledIconButton(
					colors = IconButtonDefaults.filledIconButtonColors(
						containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5f),
						contentColor = MaterialTheme.colorScheme.onSecondaryContainer
					),

					onClick = {
						coroutineScope.launch {
							onOpenDrawer()
						}
					}
				) {
					Icon(
						modifier = Modifier.padding(6.dp),
						painter = painterResource(Res.drawable.ic_account_outlined),
						contentDescription = null
					)

					AsyncImage(
						modifier = Modifier
							.clip(CircleShape)
							.fillMaxSize(),

						model = ImageRequest.Builder(LocalPlatformContext.current)
							.addLastModifiedToFileCacheKey(true)
							.data(FileKit.filesDir / "avatar.png")
							.build(),

						contentDescription = null,
						contentScale = ContentScale.Crop
					)
				}
			}
		}
	}
}

@Composable
private fun AwerySideBar(
	translucent: Boolean,
	currentTab: Int,
	onOpenTab: (Int, Routes) -> Unit
) {
	val enableNavLabels by AwerySettings.showNavigationLabels.collectAsState()
	
	Column(
		modifier = Modifier
			.background(when(translucent) {
				false -> MaterialTheme.colorScheme.surface
				true -> MaterialTheme.colorScheme.surface.let {
					if(isAmoledTheme()) it.copy(alpha = .75f) else it
				}
			}).fillMaxHeight()
			.windowInsetsPadding(WindowInsets.safeDrawing.only(
				WindowInsetsSides.Start + WindowInsetsSides.Vertical))
			.padding(horizontal = when(enableNavLabels) {
				AwerySettings.NavigationLabels.HIDE -> 8.dp
				else -> 0.dp
			}, vertical = when(enableNavLabels) {
				AwerySettings.NavigationLabels.HIDE -> 0.dp
				else -> 8.dp
			}).verticalScroll(rememberScrollState()),

		verticalArrangement = Arrangement.spacedBy(when(enableNavLabels) {
			AwerySettings.NavigationLabels.SHOW -> 16.dp
			AwerySettings.NavigationLabels.ACTIVE -> 8.dp
			else -> 0.dp
		}),

		horizontalAlignment = Alignment.CenterHorizontally
	) {
		@Composable
		fun Tab(index: Int, tab: MainRoutes) {
			WideNavigationRailItem(
				railExpanded = false,
				selected = index == currentTab,

				icon = {
					if(tab == MainRoutes.PROFILE) {
						AsyncImage(
							modifier = Modifier
								.clip(CircleShape)
								.size(24.dp),

							model = ImageRequest.Builder(LocalPlatformContext.current)
								.addLastModifiedToFileCacheKey(true)
								.data(FileKit.filesDir / "avatar.png")
								.build(),

							contentDescription = null,
							contentScale = ContentScale.Crop
						)

						return@WideNavigationRailItem
					}

					Icon(
						modifier = Modifier.size(24.dp),
						imageVector = vectorResource(tab.getIcon(index == currentTab)),
						contentDescription = null
					)
				},

				label = if(showNavLabel(index == currentTab)) {label@{
					if(tab == MainRoutes.PROFILE) {
						Text(
							text = AwerySettings.username.collectAsState().value,
							fontSize = 11.sp
						)

						return@label
					}

					Text(
						text = stringResource(tab.title),
						fontSize = 11.sp
					)
				}} else null,

				onClick = {
					onOpenTab(index, tab.route)
				}
			)
		}

		MainRoutes.entries.filter { 
			!it.desktopOnly
		}.forEachIndexed { index, tab ->
			Tab(index, tab)
		}

		Spacer(Modifier.weight(1f))

		if(Awery.platform != Platform.ANDROID) {
			// Everything is already in the drawer menu
			MainRoutes.entries.filter {
				it.desktopOnly
			}.forEachIndexed { index, tab ->
				Tab(index + MainRoutes.entries.filter { !it.desktopOnly }.size, tab)
			}
		}
	}
}

@Composable
private fun AweryBottomBar(
	currentTab: Int,
	onOpenTab: (Int, Routes) -> Unit
) {
	NavigationBar(
		containerColor = MaterialTheme.colorScheme.surface.let {
			if(isAmoledTheme()) it.copy(alpha = .75f) else it
		}
	) {
		MainRoutes.entries.filter { !it.desktopOnly }.forEachIndexed { index, tab ->
			NavigationBarItem(
				selected = index == currentTab,

				icon = {
					Icon(
						modifier = Modifier.size(24.dp),
						imageVector = vectorResource(tab.getIcon(index == currentTab)),
						contentDescription = null
					)
				},

				label = if(showNavLabel(index == currentTab)) {label@{
					Text(
						text = stringResource(tab.title),
						fontSize = 11.sp
					)
				}} else null,

				onClick = {
					onOpenTab(index, tab.route)
				}
			)
		}
	}
}

@Composable
private fun AweryNavHost(
	navigation: JetpackNavigation<Routes>,
	contentPadding: PaddingValues
) {
	val windowSize = currentWindowSize()
	val useRail = windowSize.width >= WindowSizeType.Large
	
	JetpackNavigationHost(
		modifier = Modifier.fillMaxSize(),
		navigation = navigation,

		enterTransition = {
			if(Awery.platform == Platform.DESKTOP) {
				return@JetpackNavigationHost fadeIn(tween(200))
			}

			fadeIn(tween(500)) +
					slideInHorizontally(tween(350)) { it / 2 } +
					scaleIn(tween(250), initialScale = .95f)
		},

		exitTransition = {
			if(Awery.platform == Platform.DESKTOP) {
				return@JetpackNavigationHost fadeOut(tween(200))
			}

			fadeOut(tween(500)) +
					slideOutHorizontally(tween(350)) +
					scaleOut(tween(250), targetScale = .95f)
		},

		graph = remember {
			sealedNavigationGraph { route ->
				if(route !is Routes.Intro) {
					route.Content(contentPadding)
					return@sealedNavigationGraph
				}
				
				route.Content(
					contentPadding = contentPadding.let {
						if(!route.singleStep) {
							WindowInsets.safeDrawing.asPaddingValues().add(32.dp)
						} else if(!useRail) {
							it.add(
								vertical = 16.dp,
								horizontal = niceSideInset()
							)
						} else {
							it.add(top = 16.dp)
						}
					}
				)
			}
		}
	)
}

internal interface NavigationMap {
	operator fun get(index: Int): JetpackNavigation<Routes>
}

@Composable
internal expect fun rememberNavigationMap(): NavigationMap