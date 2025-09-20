package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.adult_content
import com.mrboomdev.awery.resources.adult_content_description
import com.mrboomdev.awery.resources.enabled
import com.mrboomdev.awery.resources.hide
import com.mrboomdev.awery.resources.ic_explict_outlined
import com.mrboomdev.awery.resources.show_only_it
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemSetting
import com.mrboomdev.awery.ui.utils.add
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsCatalogPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?,
	onOpenPage: (SettingsPages) -> Unit
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text("Catalog") }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding.add(bottom = 16.dp)
		) { 
			item("adultContent") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.adultContent,
					icon = painterResource(Res.drawable.ic_explict_outlined),
					title = stringResource(Res.string.adult_content),
					description = stringResource(Res.string.adult_content_description),
					enumValues = {
						when(it) {
//							AwerySettings.AdultContent.STRICT -> "Strict mode"
							AwerySettings.AdultContent.HIDE -> stringResource(Res.string.hide)
							AwerySettings.AdultContent.SHOW -> stringResource(Res.string.enabled)
							AwerySettings.AdultContent.ONLY -> stringResource(Res.string.show_only_it)
						}
					}
				)
			}
		}
	}
}