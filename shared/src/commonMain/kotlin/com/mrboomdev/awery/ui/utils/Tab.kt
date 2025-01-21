package com.mrboomdev.awery.ui.utils

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class Tab(
	val title: StringResource,
	val activeIcon: DrawableResource,
	val inActiveIcon: DrawableResource? = null
)