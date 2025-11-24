package com.mrboomdev.awery.ui.screens.settings

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.effects.BackEffect
import com.mrboomdev.awery.ui.navigation.RouteInfo
import com.mrboomdev.awery.ui.navigation.RouteInfoEffect
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.asWindowInsets
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation

@Composable
fun SettingsScreen(
	initialPage: SettingsPages,
	contentPadding: PaddingValues
) {
	val appNavigation = Navigation.current()
	val settingsNavigation = rememberJetpackNavigation(initialPage)
	val settingsBackStack by settingsNavigation.currentBackStackFlow.collectAsState(emptyList())
	
	val windowSize = currentWindowSize()
	val isLandscape = windowSize.width >= WindowSizeType.Large 
			|| windowSize.height <= WindowSizeType.Small

	RouteInfoEffect(
		displayHeader = false
	)
	
	if(isLandscape) {
		val primaryPane = remember { SettingsPages.Main() }
		
		primaryPane.current = if(settingsBackStack.getOrNull(0) is SettingsPages.Main) {
			if(settingsBackStack.size == 1) {
				SettingsPages.Appearance
			} else settingsBackStack.getOrNull(1)
		} else settingsBackStack.getOrNull(0)
		
		if(settingsBackStack.size == 2 && settingsBackStack.firstOrNull() is SettingsPages.Main) {
			BackEffect { 
				appNavigation.safePop()
			}
		}
		
		Row(Modifier.fillMaxSize()) {
			primaryPane.Content(
				modifier = Modifier
					.fillMaxHeight()
					.widthIn(max = 300.dp)
					.fillMaxWidth(),
				
				windowInsets = contentPadding.asWindowInsets().only(
					WindowInsetsSides.Vertical + WindowInsetsSides.Start
				),
				
				onOpenPage = {
					if(settingsBackStack.last() == it) {
						return@Content
					}
					
					settingsNavigation.clear()
					settingsNavigation.push(primaryPane)
					settingsNavigation.push(it)
				},
				
				onBack = { appNavigation.safePop() }
			)
			
			JetpackNavigationHost(
				modifier = Modifier.fillMaxSize(),
				navigation = settingsNavigation,
				
				enterTransition = {
					if(Awery.platform == Platform.DESKTOP) {
						return@JetpackNavigationHost fadeIn(tween(200))
					}
					
					slideInHorizontally(tween(500)) { it / 2 } + fadeIn(tween(250)) 
				},
				
				exitTransition = { 
					slideOutHorizontally(tween(500)) { -it / 2 } + fadeOut(tween(250)) 
				},
				
				graph = remember {
					sealedNavigationGraph { page ->
						if(page is SettingsPages.Main) {
							return@sealedNavigationGraph SettingsPages.Appearance.Content(
								modifier = Modifier.fillMaxSize(),
								windowInsets = contentPadding.asWindowInsets(),
								onOpenPage = { settingsNavigation.push(it) },
								onBack = null
							)
						}

						page.Content(
							modifier = Modifier.fillMaxSize(),

							windowInsets = contentPadding.asWindowInsets().only(
								WindowInsetsSides.Vertical + WindowInsetsSides.End
							).add(WindowInsets(left = 16.dp)),

							onOpenPage = { settingsNavigation.push(it) },
							onBack = if(settingsBackStack.size > 2 ||
								(settingsBackStack.size == 2 && settingsBackStack.firstOrNull() !is SettingsPages.Main)) {{
								settingsNavigation.safePop()
							}} else null
						)
					}
				}
			)
		}
	} else {
		JetpackNavigationHost(
			modifier = Modifier.fillMaxSize(),
			navigation = settingsNavigation,
			enterTransition = { slideInHorizontally(tween(500)) { it } },
			exitTransition = { slideOutHorizontally(tween(500)) { -it } },
			graph = remember {
				sealedNavigationGraph { page ->
					if(page is SettingsPages.Main) {
						page.current = null
					}

					page.Content(
						modifier = Modifier.fillMaxSize(),
						windowInsets = contentPadding.asWindowInsets(),
						onOpenPage = { settingsNavigation.push(it) },
						onBack = {
							if(settingsBackStack.size <= 1) {
								appNavigation.safePop()
							} else settingsNavigation.safePop()
						}
					)
				}
			}
		)
	}
}