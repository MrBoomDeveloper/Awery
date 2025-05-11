package com.mrboomdev.awery.ui

import com.mrboomdev.navigation.core.TypeSafeNavigation
import kotlinx.serialization.Serializable

val Navigation = TypeSafeNavigation(Routes::class)

sealed interface Routes {
    @Serializable
    data object Main: Routes

//    @Serializable
//    data class Settings(val setting: Any)
}