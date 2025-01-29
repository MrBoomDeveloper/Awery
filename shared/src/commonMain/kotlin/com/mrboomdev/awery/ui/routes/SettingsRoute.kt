package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.core.screen.Screen
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.data.settings.get
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.platform.LocalSettingHandler
import com.mrboomdev.awery.platform.SettingHandler
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.screens.settings.SettingScreen
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.getTitleHeaderColors
import com.mrboomdev.awery.ui.screens.settings.historyState
import com.mrboomdev.awery.ui.utils.BackEffect
import com.mrboomdev.awery.utils.ReflectionSerializer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.Serializable

private class SettingSerializer: ReflectionSerializer<Setting>()

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Serializable
class SettingsRoute(
	val screen: @Serializable(SettingSerializer::class) Setting,
	val initialPath: List<String>? = null
): BaseRoute() {
	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	override fun Content() {
		val settingHandler = LocalSettingHandler.current
		val scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
		
		val areTwoPanels by remember { derivedStateOf { 
			scaffoldDirective.maxHorizontalPartitions > 1 
					|| scaffoldDirective.maxVerticalPartitions > 1 } }
		
		val navigator = rememberListDetailPaneScaffoldNavigator(
			scaffoldDirective = scaffoldDirective,
			initialDestinationHistory = buildList(1 + (initialPath?.size ?: 0)) {
				var previousPathElement = screen
				initialPath?.forEach { pathItem ->
					previousPathElement = requireNotNull(previousPathElement.items!![pathItem]) {
						"Setting item with path $initialPath was not found!"
					}
					
					add(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, previousPathElement))
				}
			}
		)
		
		val navigatorHistory = remember { navigator.historyState }
		
		BackEffect(navigator.canNavigateBack(BackNavigationBehavior.PopLatest)
				|| (!areTwoPanels && !navigatorHistory.isEmpty())) { progress ->
			try {
				progress.collect()
				navigator.navigateBack(BackNavigationBehavior.PopLatest)
				
				/*if(didOpenAnyScreen && navigatorHistory.isEmpty() && scaffoldDirective.maxHorizontalPartitions == 1) {
					navigator.navigateTo(ListDetailPaneScaffoldRole.List, settings)
					didOpenAnyScreen = false
				}*/
			} catch(_: CancellationException) {}
		}
		
		CompositionLocalProvider(LocalSettingHandler provides object : SettingHandler {
			override fun openScreen(screen: Setting) {
				if(this@SettingsRoute.screen.items?.contains(screen) == true) {
					navigator.historyState.clear()
				}
				
				navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, screen)
			}
			
			override fun handleClick(setting: Setting) {
				when(setting.type) {
					Setting.Type.SCREEN, Setting.Type.SCREEN_BOOLEAN -> openScreen(setting)
					else -> settingHandler.handleClick(setting)
				}
			}
		}) {
			val insets = WindowInsets.safeContent.asPaddingValues()
			val direction = LocalLayoutDirection.current
			
			if(areTwoPanels) {
				SettingsScreen(
					modifier = Modifier
						.padding(
							start = insets.calculateStartPadding(direction),
							end = insets.calculateEndPadding(direction)),
					screen = screen,
					navigator = navigator
				)
			} else {
				val currentScreen = navigator.currentDestination?.content ?: screen
				
				SettingScreen(
					modifier = Modifier
						.padding(
							start = insets.calculateStartPadding(direction),
							end = insets.calculateEndPadding(direction)),
					screen = currentScreen,
					header = {
						LargeTopAppBar(
							colors = getTitleHeaderColors(),
							title = {
								Text(
									text = currentScreen.title?.let { title ->
										currentScreen.takeIf { it is PlatformSetting }?.let { i18n(title) } ?: title
									} ?: "",
									
									overflow = TextOverflow.Ellipsis,
									maxLines = 1
								)
							}
						)
					}
				)
			}
		}
	}
}