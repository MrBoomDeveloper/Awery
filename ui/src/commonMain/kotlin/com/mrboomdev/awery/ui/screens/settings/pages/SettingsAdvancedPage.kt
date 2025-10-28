package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.advanced
import com.mrboomdev.awery.resources.ic_folder_open_outlined
import com.mrboomdev.awery.resources.storage
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.screens.intro.steps.IntroWelcomeStep
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.screens.settings.itemSetting
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsAdvancedPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onOpenPage: (SettingsPages) -> Unit,
	onBack: (() -> Unit)?
) {
	val navigation = Navigation.current()
	
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.advanced)) }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding
		) {
			item("storage") {
				SettingsDefaults.itemClickable(
					icon = painterResource(Res.drawable.ic_folder_open_outlined),
					title = stringResource(Res.string.storage),
					onClick = { onOpenPage(SettingsPages.Storage) }
				)
			}
			
			item("intro") {
				SettingsDefaults.itemClickable(
					title = "Launch onboarding",
					onClick = { 
						navigation.push(Routes.Intro(
							step = IntroWelcomeStep,
							singleStep = false
						)) 
					}
				)
			}
			
			item("showIds") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.showIds,
					title = "Show ids",
					description = "Extension ids and other stuff"
				)
			}
			
			item("crash") {
				SettingsDefaults.itemClickable(
					title = "Throw an Exception",
					description = "Try out an crash handler. DO NOT REPORT THESE CRASHES!",
					onClick = { throw Exception("Trying an crash handler.") }
				)
			}
		}
	}
}