package com.mrboomdev.awery.platform

import com.mrboomdev.awery.SharedPreferences
import com.mrboomdev.awery.data.AweryDB.Companion.database
import com.mrboomdev.awery.data.entity.DBList
import com.mrboomdev.awery.generated.*
import dev.mihon.injekt.patchInjekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

expect object Platform {
	val NAME: String
	val USER_AGENT: String
	val CACHE_DIRECTORY: File
	val TV: Boolean
	fun exitApp()
	fun restartApp()
	fun isRequirementMet(requirement: String): Boolean
	fun getSharedPreferences(name: String): SharedPreferences
	
	/**
	 * @return true if an system popup just appeared or false otherwise.
	 */
	fun copyToClipboard(string: String): Boolean
	
	/**
	 * Initialize platform-related stuff
	 */
	internal suspend fun platformInit()
}

private var didPlatformInit = false
val Platform.didInit get() = didPlatformInit

/**
 * Call upon opening of the application.
 */
suspend fun Platform.init() {
	if(AwerySettings.ENABLE_CRASH_HANDLER.value) {
		CrashHandler.setup()
	}
	
	if(AwerySettings.LAST_OPENED_VERSION.value < 2) {
		coroutineScope {
			launch(Dispatchers.IO) {
				database.listDao.insert(
					DBList(1, i18n(Res.string.currently_watching)),
					DBList(2, i18n(Res.string.planning_watch)),
					DBList(3, i18n(Res.string.delayed)),
					DBList(4, i18n(Res.string.completed)),
					DBList(5, i18n(Res.string.dropped)),
					DBList(6, i18n(Res.string.favourites)),
					DBList(DBList.LIST_HIDDEN, "Hidden"),
					DBList(DBList.LIST_HISTORY, "history"),
				)
				
				AwerySettings.LAST_OPENED_VERSION.value = 2
			}
		}
	}
	
	patchInjekt()
	platformInit()
	didPlatformInit = true
}

fun areRequirementsMet(requirements: Array<String>): Boolean {
	for(requirement in requirements) {
		if(!Platform.isRequirementMet(requirement)) return false
	}

	return true
}