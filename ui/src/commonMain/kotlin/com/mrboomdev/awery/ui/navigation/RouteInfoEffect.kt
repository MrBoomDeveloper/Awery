package com.mrboomdev.awery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import com.mrboomdev.navigation.core.currentNavigation
import com.mrboomdev.navigation.jetpack.LocalRouteInfo

val LocalRouteInfoCollector = 
    staticCompositionLocalOf<RouteInfoCollector> {
        throw NotImplementedError()
    }

interface RouteInfoCollector {
    fun add(routeInfo: RouteInfo)
    fun remove(routeInfo: RouteInfo)
}

@Composable
fun RouteInfoEffect(
    title: String? = null,
    displayHeader: Boolean = true,
    displayNavigationBar: Boolean = true,
    fullscreen: Boolean = false
) {
    val collector = LocalRouteInfoCollector.current
    val navigation = currentNavigation()
    val routeInfo = LocalRouteInfo.current
    
    DisposableEffect(
        title,
        displayHeader,
        displayNavigationBar,
        fullscreen,
        navigation,
        collector
    ) {
        val info = RouteInfo(
            route = routeInfo.destination as Routes,
            title,
            displayHeader,
            displayNavigationBar,
            fullscreen
        )
        
        collector.add(info)
        onDispose { collector.remove(info) }
    }
}