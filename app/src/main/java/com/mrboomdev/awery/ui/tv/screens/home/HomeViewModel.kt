package com.mrboomdev.awery.ui.tv.screens.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.app.data.db.item.DBTab
import com.mrboomdev.awery.app.data.settings.SettingsList
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.tv.components.MediaRowContent
import com.mrboomdev.awery.util.IconStateful
import com.mrboomdev.awery.util.TabsTemplate
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.util.io.FileUtil
import com.squareup.moshi.adapter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val THUMBNAIL_URL = "https://i.ibb.co/QD0b4HD/5liphf3577971.jpg"
private const val BANNER_URL = "https://i.ibb.co/YNDxjs7/b40jj2sfqfpd1.png"

class HomeViewModel : ViewModel() {
	val sectionsState = mutableStateListOf<MediaRowContent>()
	val featuredState = mutableStateListOf<CatalogMedia>()
	val tabs = mutableStateListOf<Pair<DBTab, IconStateful>>()
	var currentTab = mutableIntStateOf(-1)
	var drawerState: DrawerState = DrawerState(DrawerValue.Closed)
	var listState: LazyListState = LazyListState()

	suspend fun loadData() {
		if(tabs.isEmpty()) {
			val template = AwerySettings.TABS_TEMPLATE.value
			val icons = App.getMoshi().adapter<Map<String, IconStateful>>()
				.fromJson(FileUtil.readAssets("icons.json"))!!

			val dbTabs = if(template == "custom") App.database.tabsDao.allTabs else {
				val selected = App.getMoshi(SettingsList.ADAPTER).adapter<List<TabsTemplate>>()
					.fromJson(FileUtil.readAssets("tabs_templates.json"))!!
					.find { it.id == template }

				if(selected != null) selected.tabs else throw ZeroResultsException(
					"No templates was found with such name ($template)! Maybe it was removed."
				)
			}

			tabs.addAll(dbTabs.map {
				it to (icons[it.icon] ?: IconStateful(activeId = R.drawable.ic_view_cozy))
			})

			tabs.add(DBTab().apply {
				title = App.i18n(R.string.settings)
				id = "settings"
			} to IconStateful(activeId = R.drawable.ic_settings_filled))

			currentTab.intValue = AwerySettings.DEFAULT_HOME_TAB.value?.let { defaultTab ->
				dbTabs.find { it.id == defaultTab }
			}?.let { dbTabs.indexOf(it) } ?: 0
		}

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
}