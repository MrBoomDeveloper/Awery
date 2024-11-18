package com.mrboomdev.awery.ui.tv.screens.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.app.ExtensionsManager.loadAll
import com.mrboomdev.awery.app.data.settings.SettingsList
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.util.IconStateful
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.util.io.FileUtil.readAssets
import com.squareup.moshi.Json
import com.squareup.moshi.adapter
import java.io.Serial
import java.io.Serializable

// TODO: Remove after existing classes would be migrated to the new logic
class TabsTemplate {
	var id: String? = null
	var title: String? = null
	var icon: String? = null
	var description: String? = null
	var tabs: List<DBTab>? = null
}

// TODO: Remove after existing classes would be migrated to the new logic
class DBTab: Comparable<DBTab>, Serializable {
	var id: String = System.currentTimeMillis().toString()
	var icon: String? = null
	lateinit var title: String
	var index: Int = 0
	@Json(name = "show_end")
	var showEnd: Boolean = true
	var feeds: List<CatalogFeed>? = null

	override fun compareTo(other: DBTab): Int {
		return index.compareTo(other.index)
	}

	companion object {
		@Serial
		private val serialVersionUID: Long = 1
	}
}

class HomeViewModel : ViewModel() {
	val sectionsState = mutableStateListOf<CatalogFeed.Loaded>()
	val featuredState = mutableStateListOf<CatalogMedia>()
	val tabs = mutableStateListOf<Pair<DBTab, IconStateful>>()
	var currentTab = mutableIntStateOf(-1)
	var drawerState: DrawerState = DrawerState(DrawerValue.Closed)
	var listState: LazyListState = LazyListState()

	suspend fun loadData() {
		if(tabs.isEmpty()) {
			val template = AwerySettings.TABS_TEMPLATE.value
			val icons = getMoshi().adapter<Map<String, IconStateful>>()
				.fromJson(readAssets("icons.json"))!!

			val dbTabs = /*if(template == "custom") database.tabsDao.allTabs else*/ run {
				val selected = getMoshi(SettingsList.ADAPTER).adapter<List<TabsTemplate>>()
					.fromJson(readAssets("tabs_templates.json"))!!
					.find { it.id == template }

				if(selected != null) selected.tabs else throw ZeroResultsException(
					"No templates was found with such name ($template)! Maybe it was removed."
				)
			}!!

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

		tabs[0].first.feeds!!.loadAll().collect {
			sectionsState.add(it)
		}
	}
}