package com.mrboomdev.awery.ui.navigation

data class NavigationState(
    val route: Routes,
    val goBack: (() -> Unit)?
)