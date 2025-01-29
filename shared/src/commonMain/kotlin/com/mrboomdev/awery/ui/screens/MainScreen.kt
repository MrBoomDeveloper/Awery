package com.mrboomdev.awery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.ui.navigation.NavigationRoute
import com.mrboomdev.awery.ui.navigation.NavigationTemplates
import com.mrboomdev.awery.ui.routes.NotificationsRoute
import com.mrboomdev.awery.ui.routes.SearchRoute
import com.mrboomdev.awery.ui.routes.SettingsRoute
import com.mrboomdev.awery.ui.utils.isWidthAtLeast
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class, ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	modifier: Modifier = Modifier
) {
	var currentTab by rememberSaveable { mutableIntStateOf(0) }
	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
	val navigator = LocalNavigator.currentOrThrow
	val experience = NavigationTemplates.DANTOTSU.experience
	
	Row(modifier = modifier) {
		if(windowSizeClass.isWidthAtLeast(WindowWidthSizeClass.MEDIUM)) {
			NavigationRail(
				windowInsets = WindowInsets.safeContent
					.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
			) {
				experience.navigationBar.forEachIndexed { index, item ->
					NavigationRailItem(
						selected = index == currentTab,
						onClick = { currentTab = index },
						alwaysShowLabel = AwerySettings.NAVIGATION_LABEL.value == AwerySettings.NavigationLabelValue.ALWAYS,
						
						label = if(AwerySettings.NAVIGATION_LABEL.value !=
							AwerySettings.NavigationLabelValue.NEVER) {{ Text(item.name) }} else null,
						
						icon = {
							Icon(
								modifier = Modifier.size(25.dp),
								painter = painterResource(if(index == currentTab) {
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
					scrollBehavior = scrollBehavior,
					title = { Text("Awery") },
					
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = Color.Transparent
					),
					
					actions = {
						experience.topBar.forEachIndexed { index, item ->
							IconButton({
								when(val route = item.route) {
									is NavigationRoute.Feed -> throw UnsupportedOperationException("Feed route isn't possible!")
									
									is NavigationRoute.Settings -> {
										val settings = runBlocking { 
											Res.readBytes("files/app_settings.json").toString(Charsets.UTF_8) 
										}.let {
											@Suppress("JSON_FORMAT_REDUNDANT")
											Json {
												decodeEnumsCaseInsensitive = true
												isLenient = true
											}.decodeFromString<PlatformSetting>(it).apply { restoreValues() }
										}
										
										navigator.push(
											SettingsRoute(
												screen = settings,
												initialPath = route.initialPath
											)
										)
									}
									
									NavigationRoute.Notifications -> navigator.push(NotificationsRoute())
									NavigationRoute.Search -> navigator.push(SearchRoute())
								}
							}) {
								Icon(
									modifier = Modifier.size(25.dp),
									contentDescription = null,
									painter = painterResource(
										if(index == currentTab) {
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
					experience.navigationBar.forEachIndexed { index, item ->
						NavigationBarItem(
							selected = index == currentTab,
							onClick = { currentTab = index },
							alwaysShowLabel = AwerySettings.NAVIGATION_LABEL.value == AwerySettings.NavigationLabelValue.ALWAYS,
							
							label = if(AwerySettings.NAVIGATION_LABEL.value !=
								AwerySettings.NavigationLabelValue.NEVER) {{ Text(item.name) }} else null,
							
							icon = {
								Icon(
									modifier = Modifier.size(25.dp),
									painter = painterResource(if(index == currentTab) {
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
			when(val route = experience.navigationBar.getOrNull(currentTab)?.route) {
				is NavigationRoute.Feed -> {
					Box(Modifier.background(Color.Red).size(200.dp)) {
						Text("Feed screen")
					}
				}
				
				is NavigationRoute.Settings -> remember {
					val settings = runBlocking { 
						Res.readBytes("files/app_settings.json").toString(Charsets.UTF_8)
					}.let {
						@Suppress("JSON_FORMAT_REDUNDANT")
						Json {
							decodeEnumsCaseInsensitive = true
							isLenient = true
						}.decodeFromString<PlatformSetting>(it).apply { restoreValues() }
					}
					
					SettingsRoute(
						screen = settings,
						initialPath = route.initialPath
					)
				}
				
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