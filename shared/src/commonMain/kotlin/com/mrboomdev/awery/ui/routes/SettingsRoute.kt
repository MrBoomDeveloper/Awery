package com.mrboomdev.awery.ui.routes

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class SettingsRoute(
	val setting: Setting
): Screen {
	@Composable
	override fun Content() {
		SettingsScreen(
			setting = setting,
			settingComposable = TODO()
		)
	}
}