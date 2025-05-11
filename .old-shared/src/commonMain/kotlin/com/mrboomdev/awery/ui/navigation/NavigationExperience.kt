package com.mrboomdev.awery.ui.navigation

import org.jetbrains.compose.resources.DrawableResource

data class NavigationExperience(
	val name: String,
	val topBar: List<Item>,
	val navigationBar: List<Item>
) {
	data class Item(
		val name: String,
		val inActiveIcon: DrawableResource,
		val activeIcon: DrawableResource = inActiveIcon,
		val route: NavigationRoute
	)
}