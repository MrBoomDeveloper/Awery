package com.mrboomdev.awery.ui.navigation

import com.mrboomdev.awery.ext.data.CatalogFeed

sealed interface NavigationRoute {
	data class Feeds(val feeds: List<CatalogFeed>): NavigationRoute
	data class Feed(val feed: CatalogFeed): NavigationRoute
	data class Settings(val initialPath: List<String>? = null): NavigationRoute
	data object Search: NavigationRoute
	data object Notifications: NavigationRoute
}