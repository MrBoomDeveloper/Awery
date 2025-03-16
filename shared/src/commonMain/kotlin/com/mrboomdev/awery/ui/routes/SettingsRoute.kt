package com.mrboomdev.awery.ui.routes

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
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
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.data.settings.get
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.platform.LocalSettingHandler
import com.mrboomdev.awery.platform.SettingHandler
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.screens.settings.SettingScreen
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.historyState
import com.mrboomdev.awery.ui.screens.settings.titleHeaderColors
import com.mrboomdev.awery.ui.utils.BackEffect
import com.mrboomdev.awery.utils.SettingWrapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class SettingsRoute(
	val screen: SettingWrapper,
	val initialPath: List<String>? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsRoute.Companion.Content(args: SettingsRoute) {
	val scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
	val coroutineScope = rememberCoroutineScope()
	val settingHandler = LocalSettingHandler.current
	
	val areTwoPanels by remember { derivedStateOf { 
		scaffoldDirective.maxHorizontalPartitions > 1 
				|| scaffoldDirective.maxVerticalPartitions > 1 } }
		
	val navigator = rememberListDetailPaneScaffoldNavigator(
		scaffoldDirective = scaffoldDirective,
		initialDestinationHistory = buildList(1 + (args.initialPath?.size ?: 0)) {
			var previousPathElement = args.screen.setting
			args.initialPath?.forEach { pathItem ->
				previousPathElement = requireNotNull(previousPathElement.items!![pathItem]) {
					"Setting item with path $args.initialPath was not found!"
				}
					
				add(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, previousPathElement))
			}
		}
	)
		
	val navigatorHistory = remember { navigator.historyState }
		
	BackEffect(navigator.canNavigateBack(BackNavigationBehavior.PopLatest)
			|| (!areTwoPanels && !navigatorHistory.isEmpty())) { progress ->
		try {
			progress.collect { backEvent ->
				navigator.seekBack(
					BackNavigationBehavior.PopLatest,
					backProgressToStateProgress(
						progress = backEvent,
						scaffoldValue = navigator.scaffoldValue
					),
				)
			}
	
			navigator.navigateBack(BackNavigationBehavior.PopLatest)
		} catch(_: CancellationException) {
			withContext(NonCancellable) { navigator.seekBack(BackNavigationBehavior.PopLatest, 0f) }
		}
	}
	
	CompositionLocalProvider(LocalSettingHandler provides object : SettingHandler {
		override fun openScreen(screen: Setting) {
			if(args.screen.setting.items?.contains(screen) == true) {
				navigator.historyState.clear()
			}
				
			coroutineScope.launch {
				navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, screen)
			}
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
				screen = args.screen.setting,
				navigator = navigator
			)
		} else {
			val currentScreen = navigator.currentDestination?.contentKey ?: args.screen.setting
				
			SettingScreen(
				modifier = Modifier
					.padding(
						start = insets.calculateStartPadding(direction),
						end = insets.calculateEndPadding(direction)),
				screen = currentScreen,
				header = {
					LargeTopAppBar(
						colors = titleHeaderColors,
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

/**
 * Converts a progress value originating from a predictive back gesture into a progress value to
 * control a [ThreePaneScaffoldState].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun backProgressToStateProgress(
	progress: Float,
	scaffoldValue: ThreePaneScaffoldValue,
): Float =
	ThreePaneScaffoldPredictiveBackEasing.transform(progress) *
			when (scaffoldValue.expandedCount) {
				1 -> SinglePaneProgressRatio
				2 -> DualPaneProgressRatio
				else -> TriplePaneProgressRatio
			}

private val ThreePaneScaffoldPredictiveBackEasing: Easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
private const val SinglePaneProgressRatio: Float = 0.1f
private const val DualPaneProgressRatio: Float = 0.15f
private const val TriplePaneProgressRatio: Float = 0.2f

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private val ThreePaneScaffoldValue.expandedCount: Int
	get() {
		var count = 0
		if (primary == PaneAdaptedValue.Expanded) {
			count++
		}
		if (secondary == PaneAdaptedValue.Expanded) {
			count++
		}
		if (tertiary == PaneAdaptedValue.Expanded) {
			count++
		}
		return count
	}