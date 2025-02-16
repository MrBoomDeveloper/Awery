package com.mrboomdev.awery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mrboomdev.awery.ui.AweryRoot
import com.mrboomdev.awery.ui.routes.SplashRoute
import com.mrboomdev.awery.ui.utils.InsetsController
import com.mrboomdev.awery.ui.utils.InsetsVisibility
import com.mrboomdev.awery.ui.utils.LocalInsetsController

class MainActivity: ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		
		val initialRoute = when {
			else -> SplashRoute()
		}
		
		setContent {
			val insetsController = InsetsController()
			val controllerCompat = WindowInsetsControllerCompat(window, window.decorView)
			val insets = WindowInsetsCompat.Type.displayCutout() and WindowInsetsCompat.Type.systemBars()
			
			insetsController.stack.lastOrNull { it.visibility != null }?.also { item ->
				when(item.visibility!!) {
					InsetsVisibility.HIDDEN -> {
						controllerCompat.hide(insets)
						controllerCompat.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
					}
					
					InsetsVisibility.TRANSPARENT -> {
						controllerCompat.show(insets)
						controllerCompat.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
					}
					
					InsetsVisibility.SHOWN -> {
						controllerCompat.show(insets)
						controllerCompat.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
					}
				}
			}

			CompositionLocalProvider(
				LocalInsetsController provides insetsController
			) {
				AweryRoot(initialRoute)
			}
		}
	}
}