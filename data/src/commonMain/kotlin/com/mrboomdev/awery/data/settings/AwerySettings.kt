@file:Suppress("ClassName")

package com.mrboomdev.awery.data.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow

private const val EXTENSION_IS_ENABLED_PREFIX = "ext_enabled_"

/**
 * Application settings.
 */
object AwerySettings {
    @OptIn(ExperimentalSettingsApi::class)
	fun observeIsExtensionEnabled(id: String): Flow<Boolean> {
        return settings.getBooleanFlow(EXTENSION_IS_ENABLED_PREFIX + id, true)
    }

    fun isExtensionEnabled(id: String): Boolean {
        return settings.getBoolean(EXTENSION_IS_ENABLED_PREFIX + id, true)
    }
    
    fun setExtensionEnabled(id: String, isEnabled: Boolean) {
        settings[EXTENSION_IS_ENABLED_PREFIX + id] = isEnabled
    }
    
    val darkTheme by setting(DarkTheme.AUTO)
    val primaryColor by setting(-1L)
    val amoledTheme by setting(false)
    val adultContent by setting(AdultContent.HIDE)
    val defaultPlayerFitMode by setting(PlayerFitMode.FIT)
    val playerDoubleTapSeek by setting(15)
    val defaultMainTab by setting(MainTab.HOME)
    val showIds by setting(false)
    val username by setting("")
    val expandRepositoriesList by setting(true)
    val showNavigationLabels by setting(NavigationLabels.SHOW)

    /**
     * From 0 to 100
     */
    val wallpaperOpacity by setting(50)
    
    val introDidWelcome by setting(false)
    val introDidTheme by setting(false)
    
    enum class NavigationLabels {
        SHOW, ACTIVE, HIDE
    }

    enum class MainTab {
        HOME, SEARCH, NOTIFICATIONS, LIBRARY
    }

    enum class DarkTheme {
        AUTO, ON, OFF
    }

    enum class PlayerFitMode {
        FIT, FILL, CROP
    }
    
    enum class AdultContent {
        SHOW, HIDE, ONLY/*, STRICT*/
    }
}