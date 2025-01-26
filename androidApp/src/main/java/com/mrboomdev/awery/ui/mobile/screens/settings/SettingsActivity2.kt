package com.mrboomdev.awery.ui.mobile.screens.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.layout.windowInsetsStartWidth
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.mrboomdev.awery.app.theme.LocalAweryTheme
import com.mrboomdev.awery.app.theme.ThemeManager.setThemedContent
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ui.mobile.screens.SPLASH_EXTRA_BOOLEAN_ENABLE_COMPOSE
import com.mrboomdev.awery.ui.mobile.screens.SPLASH_EXTRA_BOOLEAN_REDIRECT_SETTINGS
import com.mrboomdev.awery.ui.mobile.screens.SplashActivity
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.historyState
import com.mrboomdev.awery.ui.utils.LocalToaster
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.utils.readAssets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class SettingsActivity2: ComponentActivity() {

	@Suppress("JSON_FORMAT_REDUNDANT")
	@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSerializationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		
		// Always true
		if("0".toInt() == 0) {
			startActivity(Intent(this, SplashActivity::class.java).apply {
				putExtra(SPLASH_EXTRA_BOOLEAN_ENABLE_COMPOSE, true)
				putExtra(SPLASH_EXTRA_BOOLEAN_REDIRECT_SETTINGS, true)
			})
			
			finish()
			return
		}

		val settings = Json {
			decodeEnumsCaseInsensitive = true
			isLenient = true
		}.decodeFromString<PlatformSetting>(readAssets("app_settings.json")).apply {
			restoreValues()
		}

		setThemedContent {
			var didOpenAnyScreen by remember { mutableStateOf(false) }
			val scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
			val navigator = rememberListDetailPaneScaffoldNavigator<Setting>(scaffoldDirective)
			val navigatorHistory = remember { navigator.historyState }
			val toaster = rememberToasterState()
			
			PredictiveBackHandler(
				(didOpenAnyScreen && !navigatorHistory.isEmpty()) || navigator.canNavigateBack(BackNavigationBehavior.PopLatest)
			) { progress ->
				try {
					progress.collect()
					navigator.navigateBack(BackNavigationBehavior.PopLatest)
					
					if(didOpenAnyScreen && navigatorHistory.isEmpty() && scaffoldDirective.maxHorizontalPartitions == 1) {
						navigator.navigateTo(ListDetailPaneScaffoldRole.List, settings)
						didOpenAnyScreen = false
					}
				} catch(_: CancellationException) {}
			}
			
			CompositionLocalProvider(LocalToaster provides toaster) {
				Row(Modifier.fillMaxSize()) {
					Spacer(Modifier.windowInsetsStartWidth(WindowInsets.safeContent))
					
					SettingsScreen(
						modifier = Modifier.weight(1f),
						screen = settings,
						navigator = navigator,
						
						/*settingComposable = { setting, onOpenScreen, isSelected ->
							MobileSetting(
								setting = setting,
								isSelected = isSelected,
	
								onOpenScreen = {
									coroutineScope.cancel()
									
									if(it is PlatformSetting && it.isLazy) {
										toast("TODO: IMPLEMENT LAZY SCREEN LOADING")
									}
	
									if(!didOpenAnyScreen) {
										didOpenAnyScreen = true
									}
	
									onOpenScreen(it)
								}
							)
						}*/
					)
					
					Spacer(Modifier.windowInsetsEndWidth(WindowInsets.safeContent))
				}
			}
			
			Toaster(
				state = toaster,
				darkTheme = LocalAweryTheme.current.isDark
			)
		}
	}
}