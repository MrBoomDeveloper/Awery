package com.mrboomdev.awery.ui.screens.intro

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.ui.navigation.RouteInfo
import com.mrboomdev.awery.ui.navigation.RouteInfoEffect
import com.mrboomdev.awery.ui.navigation.Routes
import com.mrboomdev.navigation.core.sealedNavigationGraph
import com.mrboomdev.navigation.jetpack.JetpackNavigationHost
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation

@Composable
fun IntroScreen(
    destination: Routes.Intro,
    contentPadding: PaddingValues
) {
    RouteInfoEffect(
        displayHeader = destination.singleStep,
        displayNavigationBar = destination.singleStep
    )
    
    JetpackNavigationHost(
        modifier = Modifier.fillMaxSize(),
        navigation = rememberJetpackNavigation(destination.step),
        enterTransition = { slideInHorizontally(tween(500)) { it } },
        exitTransition = { slideOutHorizontally(tween(500)) { -it } },
        graph = remember {
            sealedNavigationGraph {
                it.Content(
                    singleStep = destination.singleStep,
                    contentPadding = contentPadding
                )
            }
        }
    )
}