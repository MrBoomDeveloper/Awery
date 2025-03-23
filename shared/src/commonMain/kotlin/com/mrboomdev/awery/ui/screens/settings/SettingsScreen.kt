package com.mrboomdev.awery.ui.screens.settings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.settings.ComposableSetting
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.platform.i18n

private val defaultNavigatorClass = Class.forName(
	"androidx.compose.material3.adaptive.navigation.DefaultThreePaneScaffoldNavigator"
)

private val navigatorHistoryField = defaultNavigatorClass
	.getDeclaredField("destinationHistory").also { it.isAccessible = true }

@Suppress("UNCHECKED_CAST")
@ExperimentalMaterial3AdaptiveApi
val <T> ThreePaneScaffoldNavigator<T>.historyState
	get() = if(defaultNavigatorClass.isInstance(this)) {
		navigatorHistoryField.get(this) as SnapshotStateList<ThreePaneScaffoldDestinationItem<T>>
	} else throw UnsupportedOperationException("This navigator is not an DefaultThreePaneScaffoldNavigator!")

@OptIn(ExperimentalMaterial3Api::class)
internal val titleHeaderColors
	@Composable
	get() = TopAppBarColors(
		containerColor = Color.Transparent,
		scrolledContainerColor = Color.Transparent,
		navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
		titleContentColor = MaterialTheme.colorScheme.onSurface,
		actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		subtitleContentColor = Color.Unspecified
	)

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
fun SettingsScreen(
	modifier: Modifier = Modifier,
	screen: Setting,
	navigator: ThreePaneScaffoldNavigator<Setting> = rememberListDetailPaneScaffoldNavigator<Setting>()
) {
	val history = remember { navigator.historyState }
	
	ListDetailPaneScaffold(
		modifier = modifier,
		directive = navigator.scaffoldDirective,
		value = navigator.scaffoldValue,
		
		listPane = {
			AnimatedPane {
				if(screen is ComposableSetting) {
					screen.Content()
					return@AnimatedPane
				}
				
				SettingScreen(
					modifier = Modifier.padding(horizontal = 8.dp),
					screen = screen,
					selected = history.mapNotNull { it.contentKey },
					
					header = {
						LargeTopAppBar(
							windowInsets = WindowInsets.statusBars,
							colors = titleHeaderColors,
							title = {
								Text(
									text = screen.title?.let { title ->
										screen.takeIf { it is PlatformSetting }?.let { i18n(title) } ?: title
									} ?: ""
								)
							}
						)
					}
				)
			}
		},
		
		detailPane = {
			AnimatedPane {
				navigator.currentDestination?.contentKey?.let { currentScreen ->
					if(currentScreen is ComposableSetting) {
						currentScreen.Content()
						return@let
					}
					
					SettingScreen(
						modifier = Modifier.padding(horizontal = 8.dp),
						screen = currentScreen,
						
						header = {
							LargeTopAppBar(
								windowInsets = WindowInsets.statusBars,
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
	)
}