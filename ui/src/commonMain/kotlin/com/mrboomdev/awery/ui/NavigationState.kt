package com.mrboomdev.awery.ui

data class NavigationState(
	val route: Routes,
	val goBack: (() -> Unit)?
)