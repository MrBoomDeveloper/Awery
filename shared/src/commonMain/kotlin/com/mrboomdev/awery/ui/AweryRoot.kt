package com.mrboomdev.awery.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigatorSaver
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.FadeTransition
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.mrboomdev.awery.app.theme.AweryTheme
import com.mrboomdev.awery.app.theme.LocalAweryTheme
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.LocalSettingHandler
import com.mrboomdev.awery.platform.Platform.TV
import com.mrboomdev.awery.platform.SettingHandler
import com.mrboomdev.awery.ui.routes.BaseRoute
import com.mrboomdev.awery.ui.routes.SettingsRoute
import com.mrboomdev.awery.ui.routes.SplashRoute
import com.mrboomdev.awery.ui.utils.KSerializerNavigatorSaver
import com.mrboomdev.awery.ui.utils.LocalToaster
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun AweryRoot(initialRoute: BaseRoute = SplashRoute()) {
	val isSystemDarkMode = isSystemInDarkTheme()
	var isAmoledEnabled by remember { mutableStateOf(AwerySettings.USE_AMOLED_THEME.value) }
	
	// Light theme on tv by default is the worst thing that can happen.
	var isDarkMode by remember { mutableStateOf(
		AwerySettings.USE_DARK_THEME.value ?: if(TV) true else isSystemDarkMode) }
	
	val theme by remember { derivedStateOf {
		object : AweryTheme {
			override var isDark: Boolean
				get() = isDarkMode
				set(value) { isDarkMode = value }
			
			override var isAmoled: Boolean
				get() = isDarkMode && isAmoledEnabled
				set(value) { isAmoledEnabled = value }
			
		}
	}}
	
	CompositionLocalProvider(LocalAweryTheme provides theme) {
		AweryRootImpl {
			Surface(modifier = Modifier.fillMaxSize()) {
				CompositionLocalProvider(LocalNavigatorSaver provides KSerializerNavigatorSaver()) {
					Navigator(
						screen = initialRoute,
						disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false)
					) { navigator ->
						val toasterState = rememberToasterState()
						
						CompositionLocalProvider(LocalToaster provides toasterState) {
							CompositionLocalProvider(LocalSettingHandler provides object : SettingHandler {
								override fun openScreen(screen: Setting) {
									navigator.push(SettingsRoute(screen))
								}
								
								override fun handleClick(setting: Setting) {
									if(setting is PlatformSetting) {
										toasterState.show(
											message = "This action isn't done yet!",
											duration = 2.seconds)
										//PlatformSettingHandler.handlePlatformClick(this@SplashActivity, setting)
									} else {
										throw UnsupportedOperationException("${setting::class.qualifiedName} click handle isn't supported!")
									}
								}
							}) {
								@OptIn(ExperimentalVoyagerApi::class)
								FadeTransition(
									navigator = navigator,
									disposeScreenAfterTransitionEnd = true
								)
							}
						}
						
						Toaster(
							state = toasterState,
							darkTheme = LocalAweryTheme.current.isDark
						)
					}
				}
			}
		} 
	}
}

/**
 * A function, that provides all local platform-specific stuff such as themes
 */
@Composable
internal expect fun AweryRootImpl(content: @Composable () -> Unit)