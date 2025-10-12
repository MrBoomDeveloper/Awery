package com.mrboomdev.awery.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.mrboomdev.awery.ui.screens.intro.IntroStep
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.navigation.core.Navigation
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.bringToTop
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
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
fun App() {
    AweryTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
			val context = LocalPlatformContext.current
			val backgroundColor = MaterialTheme.colorScheme.background
			val wallpaperOpacity = AwerySettings.wallpaperOpacity.collectAsState().value / 100f
			val windowSize = currentWindowSize()

			val coroutineScope = rememberCoroutineScope()
			val drawerState = rememberDrawerState(DrawerValue.Closed)
			val toaster = remember { Toaster(maxItems = 3) }
			val navigationMap = rememberNavigationMap()
			var currentTab by rememberSaveable { mutableStateOf(0) }
			
			val currentNavigation = navigationMap[currentTab]
			val currentRoute by currentNavigation.currentDestinationFlow.collectAsState(null)
			
			val showNavigation = when(val currentRoute = currentRoute) {
				is Routes.Intro -> currentRoute.singleStep
				is Routes.Player,
				is Routes.Browser -> false
				else -> true
			}

			val showTopBar = showNavigation && when(currentRoute) {
				// TODO: Add labels to each route so that they don't have to maintain their own topbar
				is Routes.Settings, 
				is Routes.Media, 
				is Routes.Extension, 
				is Routes.ExtensionFeed, 
				is Routes.ExtensionSearch -> false
				else -> true
			}
            
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
                LocalApp provides remember { 
                    object : App {
                        override fun reloadWallpaper() {
                            wallpaperPainter.restart()
                        }
                    }
                }
            ) {
				CompositionLocalProvider(
					LocalLayoutDirection provides LayoutDirection.Rtl
				) {
					ModalNavigationDrawer(
						modifier = Modifier.fillMaxSize(),
						drawerState = drawerState,
						gesturesEnabled = showNavigation,
						drawerContent = { AweryDrawerContent(currentNavigation, drawerState, coroutineScope) }
					) {
						CompositionLocalProvider(
							LocalLayoutDirection provides LayoutDirection.Ltr
						) {
							Row {
								val useRail = windowSize.width >= WindowSizeType.Large

								AnimatedVisibility(useRail && showNavigation) {
									AwerySideBar(
										translucent = when(currentRoute) {
											is Routes.Media -> false
											else -> true
										},
										
										currentTab = currentTab,
										onOpenTab = { index, route ->
											if(index == currentTab) {
												currentNavigation.bringToTop(route)
											} else {
												currentTab = index
											}
										}
									)
								}

								Column {
									val installing by ExtensionInstaller.observeInstalling().collectAsState()
									val topAppBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()

									AnimatedVisibility(installing.isNotEmpty()) {
										Row(
											modifier = Modifier
												.background(MaterialTheme.colorScheme.primaryContainer)
												.windowInsetsPadding(WindowInsets.safeDrawing.only(
													WindowInsetsSides.Top + if(useRail) {
														WindowInsetsSides.Right
													} else WindowInsetsSides.Horizontal
												))
												.padding(16.dp),
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
											AnimatedVisibility(showTopBar && Awery.platform != Platform.DESKTOP) {
												AweryTopBar(
													topAppBarBehavior = topAppBarBehavior,
													showSearch = currentRoute == Routes.Search,
													onOpenDrawer = { drawerState.open() }
												)
											}
										},

										bottomBar = {
											AnimatedVisibility(!useRail && showNavigation) {
												AweryBottomBar(
													currentTab = currentTab,
													onOpenTab = { index, route ->
														if(index == currentTab) {
															currentNavigation.bringToTop(route)
														} else {
															currentTab = index
														}
													}
												)
											}
										}
									) { contentPadding ->
										AweryNavHost(
											navigation = currentNavigation,
											contentPadding = contentPadding
										)
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
								navigation.push(Routes.Intro(
									step = IntroStep.UserCreation,
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
	onOpenDrawer: suspend () -> Unit
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
			Image(
				modifier = Modifier.size(36.dp),
				painter = painterResource(Res.drawable.logo_awery),
				contentDescription = null
			)

			if(!showSearch || windowSize.width >= WindowSizeType.Large) {
				Text(
					style = MaterialTheme.typography.titleLarge,
					text = "Awery"
				)
			}

			Box(Modifier.weight(1f)) {
				if(!showSearch) return@Box

				val spacing = when {
					windowSize.width >= WindowSizeType.Large -> 32.dp
					windowSize.width >= WindowSizeType.Medium -> 16.dp
					else -> 8.dp
				}

				Row(
					modifier = Modifier
						.padding(horizontal = spacing)
						.clip(RoundedCornerShape(12.dp))
						.background(MaterialTheme.colorScheme.surfaceContainerHighest.let {
							if(AwerySettings.amoledTheme.collectAsState().value) it.copy(alpha = .75f) else it
						})
						.border(.5.dp, Color(0x22ffffff), RoundedCornerShape(12.dp))
						.widthIn(max = 400.dp)
						.fillMaxWidth()
						.height(48.dp)
				) {
					val focusRequester = remember { FocusRequester() }
					val query by App.searchQuery.collectAsState()

					BasicTextField(
						modifier = Modifier
							.fillMaxSize()
							.padding(horizontal = 16.dp)
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
												.heightIn(max = 42.dp)
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
				when(val route = route) {
					Routes.Home -> Routes.Home.Content(contentPadding = contentPadding)
					Routes.Search -> Routes.Search.Content(contentPadding = contentPadding)
					Routes.Notifications -> Routes.Notifications.Content(contentPadding = contentPadding)
					Routes.Library -> Routes.Library.Content(contentPadding = contentPadding)
					is Routes.Browser -> route.Content(contentPadding = contentPadding)
					is Routes.Extension -> route.Content(contentPadding = contentPadding)
					is Routes.ExtensionFeed -> route.Content(contentPadding	= contentPadding)
					is Routes.ExtensionSearch -> route.Content(contentPadding = contentPadding)
					
					is Routes.Intro -> route.Content(
						contentPadding = contentPadding.let { 
							if(!route.singleStep ) {
								it.add(32.dp)
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
					
					is Routes.Media -> route.Content(contentPadding = contentPadding)
					is Routes.Player -> route.Content(contentPadding = contentPadding)
					is Routes.Settings -> route.Content(contentPadding = contentPadding)
				}
			}
		}
	)
}

internal interface NavigationMap {
	operator fun get(index: Int): JetpackNavigation<Routes>
}

@Composable
internal expect fun rememberNavigationMap(): NavigationMap