package com.mrboomdev.awery.ui.navigation

import androidx.compose.runtime.Composable
import com.mrboomdev.navigation.jetpack.JetpackNavigation

internal interface NavigationMap {
    operator fun get(index: Int): JetpackNavigation<Routes>
}

@Composable
internal expect fun rememberNavigationMap(): NavigationMap