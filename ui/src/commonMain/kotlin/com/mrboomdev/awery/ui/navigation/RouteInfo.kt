package com.mrboomdev.awery.ui.navigation

/**
 * Used for the host component to know what decorations in should to apply to the screen.
 */
data class RouteInfo(
    val title: String?,
    val displayHeader: Boolean,
    val displayNavigationBar: Boolean,
    // TODO: Actually use this variable on mobile and desktop
    val fullscreen: Boolean
)