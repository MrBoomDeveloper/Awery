package com.mrboomdev.awery.ui.routes

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ui.screens.SplashScreen
import com.mrboomdev.navigation.core.TypeSafeNavigation
import kotlinx.serialization.Serializable

val AweryNavigation = TypeSafeNavigation(Routes::class)

@Serializable
sealed interface Routes {
    data object Splash: Routes
    data object Main: Routes
    data class Settings(val setting: Setting): Routes
}

@Composable
fun Routes.Splash.Content() = SplashScreen()