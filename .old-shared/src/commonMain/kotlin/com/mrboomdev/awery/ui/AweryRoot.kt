package com.mrboomdev.awery.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.mrboomdev.awery.app.theme.AweryTheme
import com.mrboomdev.awery.app.theme.LocalAweryTheme
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.LocalSettingHandler
import com.mrboomdev.awery.platform.Platform.TV
import com.mrboomdev.awery.platform.SettingHandler
import com.mrboomdev.awery.ui.navigation.LocalNavHostController
import com.mrboomdev.awery.ui.routes.Content
import com.mrboomdev.awery.ui.routes.MainRoute
import com.mrboomdev.awery.ui.routes.MediaRoute
import com.mrboomdev.awery.ui.routes.Routes
import com.mrboomdev.awery.ui.routes.SearchRoute
import com.mrboomdev.awery.ui.routes.SettingsRoute
import com.mrboomdev.awery.ui.routes.SplashRoute
import com.mrboomdev.awery.ui.utils.LocalToaster
import com.mrboomdev.awery.ui.utils.buildNavTypes
import com.mrboomdev.awery.utils.SettingWrapper
import com.mrboomdev.awery.utils.wrap
import com.mrboomdev.navigation.core.Navigation
import com.mrboomdev.navigation.jetpack.JetpackNavigation
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigationState
import kotlin.time.Duration.Companion.seconds

@Composable
fun AweryRoot(initialRoute: Any = SplashRoute) {
	val toasterState = rememberToasterState()
	val navState = rememberJetpackNavigationState()
	
	// Light theme on tv by default is the worst thing that can happen.
	val isDarkMode = AwerySettings.USE_DARK_THEME.value ?: if(TV) true else isSystemInDarkTheme()
	val isAmoledEnabled = isDarkMode && AwerySettings.USE_AMOLED_THEME.value
	
	val theme by remember { derivedStateOf {
		object : AweryTheme {
			override val isDark get() = isDarkMode
			override val isAmoled get() = isAmoledEnabled
			
		}
	}}
	
	val defaultSettingHandler = remember {
		object : SettingHandler {
			override fun openScreen(screen: Setting) {
				navState.push(Routes.Settings(screen))
			}
			
			override fun handleClick(setting: Setting) {
				if(setting is PlatformSetting) {
					toasterState.show(
						message = "This action isn't done yet!",
						duration = 2.seconds
					)
					
					//PlatformSettingHandler.handlePlatformClick(this@SplashActivity, setting)
				} else {
					throw UnsupportedOperationException("${setting::class.qualifiedName} click handle isn't supported!")
				}
			}
		}
	}
	
	CompositionLocalProvider(
		LocalAweryTheme provides theme,
		LocalNavHostController provides navState,
		LocalToaster provides toasterState,
		LocalSettingHandler provides defaultSettingHandler
	) {
		AweryRootImpl {
			Surface(modifier = Modifier.fillMaxSize()) {
				JetpackNavigation(
					state = navState,
					initialRoute = initialRoute
				) {
					route<Routes.Splash> { Routes.Splash.Content() }
					route<Routes.Main> { Routes.Main.Content() }

					route<Routes.Search> {
						SearchRoute.Content(it)
					}

					route<Routes.Media>(
						typeMap = buildNavTypes { 
							navType<CatalogMedia>() 
						}
					) { MediaRoute.Content(it.toRoute<MediaRoute>()) }

					route<Routes.Settings> { Routes.Settings.Content(it) }
				}
				
				Toaster(
					state = toasterState,
					darkTheme = LocalAweryTheme.current.isDark
				)
			}
		} 
	}
}

/**
 * A function, that provides all local platform-specific stuff such as themes
 */
@Composable
internal expect fun AweryRootImpl(content: @Composable () -> Unit)