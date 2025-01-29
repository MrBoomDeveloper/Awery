package com.mrboomdev.awery.ui.navigation

import com.mrboomdev.awery.ext.data.Setting

sealed interface NavigationRoute {
	data class Feed(val filters: List<Setting>? = null): NavigationRoute
	data class Settings(val initialPath: List<String>? = null): NavigationRoute
	data object Search: NavigationRoute
	data object Notifications: NavigationRoute
}