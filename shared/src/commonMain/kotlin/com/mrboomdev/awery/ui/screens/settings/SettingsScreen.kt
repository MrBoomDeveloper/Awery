package com.mrboomdev.awery.ui.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.platform.PlatformResources.i18n
import com.mrboomdev.awery.platform.PlatformSetting

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
@Composable
private fun getTitleHeaderColors() = TopAppBarColors(
	containerColor = Color.Transparent,
	scrolledContainerColor = Color.Transparent,
	navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
	titleContentColor = MaterialTheme.colorScheme.onSurface,
	actionIconContentColor = MaterialTheme.colorScheme.onSurface
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	modifier: Modifier = Modifier,
	setting: Setting,
	settingComposable: @Composable (
		setting: Setting,
		onOpenScreen: (Setting) -> Unit,
		isSelected: Boolean
	) -> Unit,
	navigator: ThreePaneScaffoldNavigator<Setting> = rememberListDetailPaneScaffoldNavigator<Setting>()
) {
	val history = remember { navigator.historyState }

	ListDetailPaneScaffold(
		modifier = modifier,
		directive = navigator.scaffoldDirective,
		value = navigator.scaffoldValue,

		listPane = {
			SettingScreen(
				screen = setting,
				selected = history.mapNotNull { it.content },
				setting = settingComposable,

				header = {
					MediumTopAppBar(
						colors = getTitleHeaderColors(),
						title = {
							Text(
								text = setting.title?.let { title ->
									setting.takeIf { it is PlatformSetting }?.let { i18n(title) } ?: title
								} ?: "",

								overflow = TextOverflow.Ellipsis,
								maxLines = 1
							)
						}
					) 
				},

				onOpenScreen = {
					navigator.historyState.clear()
					navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
				}
			)
		},

		detailPane = {
			navigator.currentDestination?.content?.let { currentScreen ->
				SettingScreen(
					screen = currentScreen,
					setting = settingComposable,

					header = {
						MediumTopAppBar(
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
					},

					onOpenScreen = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) }
				)
			}
		}
	)
}