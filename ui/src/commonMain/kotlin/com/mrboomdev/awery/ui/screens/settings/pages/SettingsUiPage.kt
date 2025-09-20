package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.home
import com.mrboomdev.awery.resources.ic_home_outlined
import com.mrboomdev.awery.resources.library
import com.mrboomdev.awery.resources.notifications
import com.mrboomdev.awery.resources.search
import com.mrboomdev.awery.resources.ui
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemSetting
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsUiPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.ui)) }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding
		) {
			item("startPage") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.defaultMainTab,
					icon = painterResource(Res.drawable.ic_home_outlined),
					title = "Start page",
					enumValues = {
						when(it) {
							AwerySettings.MainTab.HOME -> stringResource(Res.string.home)
							AwerySettings.MainTab.SEARCH -> stringResource(Res.string.search)
							AwerySettings.MainTab.NOTIFICATIONS -> stringResource(Res.string.notifications)
							AwerySettings.MainTab.LIBRARY -> stringResource(Res.string.library)
						}
					}
				)
			}

			item("navigationLabels") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.showNavigationLabels,
					title = "Show navigation labels",
					enumValues = {
						when(it) {
							AwerySettings.NavigationLabels.SHOW -> "Always show"
							AwerySettings.NavigationLabels.ACTIVE -> "Only active"
							AwerySettings.NavigationLabels.HIDE -> "Don't show"
						}
					}
				)
			}
		}
	}
}