package com.mrboomdev.awery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.data.settings.Settings
import com.mrboomdev.awery.ui.screens.main.MainScreen
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigationState

@Composable
fun App(initialRoute: Routes) {
    val navigationState = rememberJetpackNavigationState()

    LaunchedEffect(Settings.darkTheme.value) {
        // TODO: Change status bar color depending on the theme
    }

    AweryTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // TODO: Fix this warning in the next library update
            @Suppress("MISSING_DEPENDENCY_CLASS_IN_EXPRESSION_TYPE")
            JetpackNavigation<Routes>(
                modifier = Modifier.fillMaxSize(),
                state = navigationState,
                initialRoute = initialRoute
            ) {
                route<Routes.Main> { MainScreen() }
            }
        }
    }
}