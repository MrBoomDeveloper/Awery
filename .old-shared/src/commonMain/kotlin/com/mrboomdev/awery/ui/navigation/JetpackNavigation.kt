package com.mrboomdev.awery.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavHostController = staticCompositionLocalOf<NavHostController> { 
	throw NotImplementedError()
}