package com.mrboomdev.awery.ui.mobile.screens.settings

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.theme.ThemeManager.setThemedContent
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.platform.PlatformSetting
import com.mrboomdev.awery.ui.mobile.components.MobileSetting
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.historyState
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.readAssets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File

class SettingsActivity2: ComponentActivity() {

	@Suppress("JSON_FORMAT_REDUNDANT")
	@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSerializationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		val settings = Json {
			decodeEnumsCaseInsensitive = true
			isLenient = true
		}.decodeFromString<PlatformSetting>(
			File("app_settings.json").readAssets()
		).apply {
			restoreValues()
		}

		setThemedContent {
			val scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
			val navigator = rememberListDetailPaneScaffoldNavigator<Setting>(scaffoldDirective)
			val navigatorHistory = remember { navigator.historyState }
			var didOpenAnyScreen by remember { mutableStateOf(false) }

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
	
			Row(Modifier.fillMaxSize()) {
				Spacer(Modifier.windowInsetsStartWidth(WindowInsets.safeContent))

				SettingsScreen(
                    modifier = Modifier.weight(1f),
                    setting = settings,
                    navigator = navigator,

                    settingComposable = { setting, onOpenScreen, isSelected ->
                        MobileSetting(
                            setting = setting,
                            isSelected = isSelected,

                            onOpenScreen = {
                                if(it is PlatformSetting && it.isLazy) {
                                    toast("TODO: IMPLEMENT LAZY SCREEN LOADING")
                                }

                                if(!didOpenAnyScreen) {
                                    didOpenAnyScreen = true
                                }

                                onOpenScreen(it)
                            }
                        )
                    }
				)

				Spacer(Modifier.windowInsetsEndWidth(WindowInsets.safeContent))
			}
		}
	}
}