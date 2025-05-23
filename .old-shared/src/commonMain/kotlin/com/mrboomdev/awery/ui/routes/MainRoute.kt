package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.ui.navigation.NavigationRoute
import com.mrboomdev.awery.ui.navigation.NavigationTemplates
import com.mrboomdev.awery.ui.pane.FeedsPane
import com.mrboomdev.awery.ui.pane.FeedsPaneState
import com.mrboomdev.awery.ui.utils.WINDOW_SIZE_MEDIUM
import com.mrboomdev.awery.ui.utils.compareTo
import com.mrboomdev.awery.ui.utils.plus
import com.mrboomdev.awery.utils.wrap
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource

@Composable
expect fun Routes.Main.Content(viewModel: MainRouteViewModel = viewModel())

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DefaultMainRouteContent(viewModel: MainRouteViewModel = viewModel()) {
	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
	val navigator = AweryNavigation.current()
	
	Row(modifier = Modifier.fillMaxSize()) {
		if(windowSizeClass >= WINDOW_SIZE_MEDIUM) {
			NavigationRail(
				windowInsets = WindowInsets.safeContent
					.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
			) {
				viewModel.experience.navigationBar.forEachIndexed { index, item ->
					NavigationRailItem(
						selected = viewModel.currentTab == index,
						onClick = { viewModel.currentTab = index },
						alwaysShowLabel = AwerySettings.NAVIGATION_LABEL.state.value == AwerySettings.NavigationLabelValue.ALWAYS,
						
						label = if(AwerySettings.NAVIGATION_LABEL.state.value !=
							AwerySettings.NavigationLabelValue.NEVER) {{ Text(item.name) }} else null,
							
						icon = {
							Icon(
								modifier = Modifier.size(25.dp),
								painter = painterResource(if(index == viewModel.currentTab) {
									item.activeIcon
								} else item.inActiveIcon),
								contentDescription = null
							)
						}
					)
				}
			}
		}
			
		Scaffold(
			modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
				
			topBar = {
				TopAppBar(
					windowInsets = WindowInsets.safeContent
						.only(WindowInsetsSides.Top + if(windowSizeClass >= WINDOW_SIZE_MEDIUM) {
							WindowInsetsSides.End
						} else WindowInsetsSides.Horizontal),
						
					scrollBehavior = scrollBehavior,
						
					title = { 
						Row(
							verticalAlignment = Alignment.CenterVertically
						) {
							Image(
								modifier = Modifier.size(40.dp),
								painter = painterResource(Res.drawable.logo_awery),
								contentDescription = null
							)
								
							Spacer(Modifier.width(16.dp))
								
							Text(
								fontWeight = FontWeight.SemiBold,
								text = "Awery"
							)
						}
					},
						
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = Color.Transparent,
						scrolledContainerColor = MaterialTheme.colorScheme.surface
					),
						
					actions = {
						viewModel.experience.topBar.forEachIndexed { index, item ->
							IconButton({
								when(val route = item.route) {
									NavigationRoute.Notifications -> navigator.push(NotificationsRoute())
									NavigationRoute.Search -> navigator.push(SearchRoute())
									
									is NavigationRoute.Feeds -> 
										throw UnsupportedOperationException("Feeds route isn't possible!")
										
									is NavigationRoute.Feed ->
										throw UnsupportedOperationException("Feed route isn't possible!")
										
									is NavigationRoute.Settings -> navigator.push(
										SettingsRoute(
											screen = viewModel.appSettings.wrap(),
											initialPath = route.initialPath
										)
									)
								}
							}) {
								Icon(
									modifier = Modifier.size(25.dp),
									contentDescription = null,
									painter = painterResource(
										if(index == viewModel.currentTab) {
											item.activeIcon
										} else item.inActiveIcon
									)
								)
							}
						}
					}
				)
			},
				
			bottomBar = if(windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {{
				NavigationBar {
					viewModel.experience.navigationBar.forEachIndexed { index, item ->
						NavigationBarItem(
							selected = viewModel.currentTab == index,
							onClick = { viewModel.currentTab = index },
							alwaysShowLabel = AwerySettings.NAVIGATION_LABEL.state.value == AwerySettings.NavigationLabelValue.ALWAYS,
								
							label = if(AwerySettings.NAVIGATION_LABEL.state.value !=
								AwerySettings.NavigationLabelValue.NEVER) {{ Text(item.name) }} else null,
								
							icon = {
								Icon(
									modifier = Modifier.size(25.dp),
									painter = painterResource(if(index == viewModel.currentTab) {
										item.activeIcon
									} else item.inActiveIcon),
									contentDescription = null
								)
							}
						)
					}
				}
			}} else {{}}
		) {
			when(val route = viewModel.experience.navigationBar.getOrNull(viewModel.currentTab)?.route) {
				is NavigationRoute.Feeds -> {
					val padding = if(windowSizeClass >= WINDOW_SIZE_MEDIUM) {
						WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
					} else {
						WindowInsets.safeDrawing.only(
							WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
					}.asPaddingValues() + PaddingValues(
						start = 8.dp,
						top = 64.dp,
						end = 4.dp,
						bottom = 128.dp
					)
						
					val state = viewModel.feedsPaneStates.getOrPut(route) {
						FeedsPaneState(coroutineScope = viewModel.viewModelScope).apply {
							load(route.feeds)
						}
					}
						
					FeedsPane(
						modifier = Modifier.fillMaxSize(),
						contentPadding = padding,
						state = state,
						onReload = { state.load(route.feeds) },
						onMediaClick = { navigator.push(MediaRoute(it)) },
							
						onSectionClick = { feed, results ->
								
						}
					)
				}
					
				is NavigationRoute.Feed -> TODO()
					
				is NavigationRoute.Settings -> SettingsRoute.Content(SettingsRoute(
					screen = viewModel.appSettings.wrap(),
					initialPath = route.initialPath
				))
					
				NavigationRoute.Notifications -> {
					Text("Notifications screen")
				}
					
				NavigationRoute.Search -> {
					Text("Search screen")
				}
					
				null -> {
					Text("You have no tabs created.")
				}
			}
		}
	}
}

class MainRouteViewModel(handle: SavedStateHandle): ViewModel() {
	val experience = NavigationTemplates.AWERY.experience
	var currentTab by handle.saveable { mutableIntStateOf(0) }
	val feedsPaneStates = mutableStateMapOf<NavigationRoute, FeedsPaneState>()

	val appSettings by lazy { runBlocking {
		PlatformSetting.fromAssets("files/app_settings.json")
	} }
}