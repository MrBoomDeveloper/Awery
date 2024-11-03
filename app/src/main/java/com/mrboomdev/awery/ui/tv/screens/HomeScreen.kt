package com.mrboomdev.awery.ui.tv.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemColors
import androidx.tv.material3.Text
import androidx.tv.material3.contentColorFor
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.app.App.Companion.i18n
import com.mrboomdev.awery.app.data.db.item.DBTab
import com.mrboomdev.awery.app.data.settings.SettingsList
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.tv.components.MediaRowContent
import com.mrboomdev.awery.util.IconStateful
import com.mrboomdev.awery.util.TabsTemplate
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.util.io.FileUtil.readAssets
import com.squareup.moshi.adapter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private const val THUMBNAIL_URL = "https://i.ibb.co/QD0b4HD/5liphf3577971.jpg"
private const val BANNER_URL = "https://i.ibb.co/YNDxjs7/b40jj2sfqfpd1.png"

@Serializable
object HomeScreenArgs

@Composable
fun HomeScreen(
	navController: NavController
) {
	val sectionsState = remember { mutableStateListOf<MediaRowContent>() }
	val featuredState = remember { mutableStateListOf<CatalogMedia>() }
	val tabs = remember { mutableStateListOf<Pair<DBTab, IconStateful>>() }
	val savedDefaultTab = remember { AwerySettings.DEFAULT_HOME_TAB.value }

	LaunchedEffect("HomeScreenEffect") {
		/*if(AwerySettings.AUTO_CHECK_APP_UPDATE.value) {
			launch(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
				Log.e(TAG, "Failed to check for updates!", t)
			}) {
				val update = UpdatesManager.fetchLatestAppUpdate()
				UpdatesManager.showUpdateDialog(LocalContext.current.activity, update)
			}
		}*/

		loadData(sectionsState, featuredState, tabs)
	}

	Row {
		var selectedIndex by remember { mutableIntStateOf(0) }

		NavigationDrawer(drawerContent = {
			Column(
				horizontalAlignment = Alignment.Start,
				verticalArrangement = Arrangement.spacedBy(10.dp),
				modifier = Modifier
					.fillMaxHeight()
					.padding(12.dp)
					.selectableGroup()
			) {
				tabs.forEachIndexed { index, (tab, icon) ->
					NavigationDrawerItem(
						selected = selectedIndex == index,
						colors = NavigationDrawerItemColors(
							containerColor = Color.Transparent,
							contentColor = Color(0xFFFFFFFF),
							inactiveContentColor = Color(0x56FFFFFF),
							focusedContainerColor = Color(0x3EFFFFFF),
							focusedContentColor = contentColorFor(MaterialTheme.colorScheme.inverseSurface),
							pressedContainerColor = Color(0x3EFFFFFF),
							pressedContentColor = contentColorFor(MaterialTheme.colorScheme.inverseSurface),
							selectedContainerColor = Color(0x14FFFFFF),
							selectedContentColor = Color(0xffffffff),
							disabledContainerColor = Color.Transparent,
							disabledContentColor = MaterialTheme.colorScheme.onSurface,
							disabledInactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
							focusedSelectedContainerColor = Color(0x3EFFFFFF),
							focusedSelectedContentColor = Color(0xffffffff),
							pressedSelectedContainerColor = MaterialTheme.colorScheme.inverseSurface,
							pressedSelectedContentColor = contentColorFor(MaterialTheme.colorScheme.inverseSurface)
						),

						onClick = {
							if(tab.id == "settings") {
								navController.navigate(SettingsScreenArgs)
								return@NavigationDrawerItem
							}

							selectedIndex = index
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
			FeedsScreen(
				sections = sectionsState,
				featuredItems = featuredState,
				onItemSelected = {
					navController.navigate(MediaScreenArgs(media = it))
				}
			)
		}
	}
}

@OptIn(ExperimentalStdlibApi::class)
private suspend fun loadData(
	sectionsState: MutableList<MediaRowContent>,
	featuredState: MutableList<CatalogMedia>,
	tabsState: MutableList<Pair<DBTab, IconStateful>>
) {
	val template = AwerySettings.TABS_TEMPLATE.value
	val icons = getMoshi().adapter<Map<String, IconStateful>>()
		.fromJson(readAssets("icons.json"))!!

	val tabs = if(template == "custom") database.tabsDao.allTabs
	else {
		val selected = getMoshi(SettingsList.ADAPTER).adapter<List<TabsTemplate>>()
			.fromJson(readAssets("tabs_templates.json"))!!
			.find { it.id == template }

		if(selected != null) selected.tabs else throw ZeroResultsException(
			"No templates was found with such name ($template)! Maybe it was removed.")
	}

	tabsState.addAll(tabs.map {
		it to (icons[it.icon] ?: IconStateful(activeId = R.drawable.ic_view_cozy))
	})

	tabsState.add(DBTab().apply {
		title = i18n(R.string.settings)
		id = "settings"
	} to IconStateful(activeId = R.drawable.ic_settings_filled))

	coroutineScope {
		launch {
			for(i in 1..10) {
				delay(3000)

				featuredState.add(
					CatalogMedia(
					globalId = "a;;;a;;;a",
					titles = arrayOf("Attack on Titan"),
					banner = BANNER_URL,
					poster = THUMBNAIL_URL
				)
				)
			}
		}

		launch {
			for(i in 1..10) {
				delay(3000)

				sectionsState.add(MediaRowContent("Feed $i", mutableListOf<CatalogMedia>().apply {
					for(j in 1..20) {
						add(
							CatalogMedia(
							globalId = "a;;;a;;;a",
							titles = arrayOf("Attack on Titan"),
							banner = BANNER_URL,
							poster = THUMBNAIL_URL
						)
						)
					}
				}.toList()))
			}
		}
	}
}