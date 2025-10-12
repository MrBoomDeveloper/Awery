package com.mrboomdev.awery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mrboomdev.navigation.core.InternalNavigationApi
import com.mrboomdev.navigation.core.currentNavigationOrNull
import com.mrboomdev.navigation.jetpack.JetpackNavigation

@OptIn(InternalNavigationApi::class)
@Composable
internal actual fun rememberNavigationMap(): NavigationMap {
	val parent = currentNavigationOrNull()

	val navigators = remember(parent) {
		mutableMapOf<Int, JetpackNavigation<Routes>>()
	}

	return remember(navigators) {
		object : NavigationMap {
			override fun get(index: Int): JetpackNavigation<Routes> {
				return navigators.getOrPut(index) {
					JetpackNavigation(
						initialRoute = MainRoutes.entries[index].route,
						parent = parent
					)
				}
			}
		}
	}
}