package com.mrboomdev.awery.ui.tv.screens.home

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemColors
import androidx.tv.material3.Text
import androidx.tv.material3.contentColorFor
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.AwerySettings
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity
import com.mrboomdev.awery.ui.tv.Screens
import com.mrboomdev.awery.ui.tv.components.FeedsGroup
import com.mrboomdev.awery.util.IconStateful
import com.mrboomdev.awery.util.extensions.startActivity

@Composable
fun HomeScreen(
	navController: NavController,
	viewModel: HomeViewModel = viewModel()
) {
	LaunchedEffect(true) {
		viewModel.loadTabsList()
	}

	LaunchedEffect(viewModel.currentTab.intValue) {
		viewModel.loadTabIfRequired(viewModel.currentTab.intValue)
	}

	Row {
		NavigationDrawer(drawerState = viewModel.drawerState, drawerContent = {
			Column(
				horizontalAlignment = Alignment.Start,
				verticalArrangement = Arrangement.spacedBy(10.dp),
				modifier = Modifier
					.fillMaxHeight()
					.padding(12.dp)
					.selectableGroup()
			) {
				viewModel.tabs.forEachIndexed { index, (tab, icon) ->
					NavigationDrawerItem(
						selected = viewModel.currentTab.intValue == index,
						colors = NavigationDrawerItemColors(
							containerColor = Color.Transparent,
							contentColor = Color.White,
							inactiveContentColor = Color(0x56FFFFFF),
							focusedContainerColor = Color.White,
							focusedContentColor = Color.Black,
							pressedContainerColor = Color(0x3EFFFFFF),
							pressedContentColor = contentColorFor(MaterialTheme.colorScheme.inverseSurface),
							selectedContainerColor = Color(0x14FFFFFF),
							selectedContentColor = Color.White,
							disabledContainerColor = Color.Transparent,
							disabledContentColor = MaterialTheme.colorScheme.onSurface,
							disabledInactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
							focusedSelectedContainerColor = Color.White,
							focusedSelectedContentColor = Color.Black,
							pressedSelectedContainerColor = MaterialTheme.colorScheme.inverseSurface,
							pressedSelectedContentColor = contentColorFor(MaterialTheme.colorScheme.inverseSurface)
						),

						onClick = {
							if(tab.id == "settings") {
								if(AwerySettings.EXPERIMENT_SETTINGS2.value == true) {
									navController.navigate(Screens.Settings)
									return@NavigationDrawerItem
								}

								getAnyActivity<Activity>()!!.startActivity(SettingsActivity::class)
								return@NavigationDrawerItem
							}

							viewModel.currentTab.intValue = index
						},

						leadingContent = {
							Icon(
								painter = painterResource(icon.getResourceId(IconStateful.State.ACTIVE)),
								contentDescription = null
							)
						}
					) {
						Text(tab.title)
					}
				}
			}
		}) {
			val currentTab = viewModel.tabsState.getOrNull(viewModel.currentTab.intValue)

			FeedsGroup(
				listState = viewModel.listState,
				isLoading = currentTab?.isLoading?.value ?: true,
				sections = currentTab?.feeds,
				failedSections = currentTab?.failedFeeds,
				onItemSelected = {
					navController.navigate(Screens.Media(media = it))
				}
			)
		}
	}
}