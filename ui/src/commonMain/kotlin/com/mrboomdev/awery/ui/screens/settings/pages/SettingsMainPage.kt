package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.about
import com.mrboomdev.awery.resources.advanced
import com.mrboomdev.awery.resources.app_language
import com.mrboomdev.awery.resources.content
import com.mrboomdev.awery.resources.extensions
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.ic_collections_bookmark_outlined
import com.mrboomdev.awery.resources.ic_dashboard_outlined
import com.mrboomdev.awery.resources.ic_dev_outlined
import com.mrboomdev.awery.resources.ic_explore_outlined
import com.mrboomdev.awery.resources.ic_extension_outlined
import com.mrboomdev.awery.resources.ic_info_outlined
import com.mrboomdev.awery.resources.ic_language
import com.mrboomdev.awery.resources.ic_palette_outlined
import com.mrboomdev.awery.resources.ic_video_settings_outlined
import com.mrboomdev.awery.resources.library
import com.mrboomdev.awery.resources.player
import com.mrboomdev.awery.resources.settings
import com.mrboomdev.awery.resources.ui
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.popups.LanguageDialog
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.screens.settings.itemDialog
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.exclude
import com.mrboomdev.awery.ui.utils.plus
import com.mrboomdev.awery.ui.utils.top
import com.mrboomdev.navigation.core.safePop
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	current: SettingsPages?,
	onOpenPage: (SettingsPages) -> Unit,
	onBack: (() -> Unit)?
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.settings)) }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding.add(bottom = 16.dp)
		) {
			if(Awery.platform == Platform.ANDROID) {
				item("language") {
					SettingsDefaults.itemDialog(
						icon = painterResource(Res.drawable.ic_language),
						title = stringResource(Res.string.app_language),
						dialog = { LanguageDialog(::dismiss) }
					)
				}
			}
			
			item("appearance") {
				SettingsDefaults.itemClickable(
					title = "Appearance",
					icon = painterResource(Res.drawable.ic_palette_outlined),
					checked = current == SettingsPages.Appearance,
					onClick = { onOpenPage(SettingsPages.Appearance) }
				)
			}

			item("ui") {
				SettingsDefaults.itemClickable(
					title = stringResource(Res.string.ui),
					icon = painterResource(Res.drawable.ic_dashboard_outlined),
					checked = current == SettingsPages.Ui,
					onClick = { onOpenPage(SettingsPages.Ui) }
				)
			}

			item("catalog") {
				SettingsDefaults.itemClickable(
					title = "Catalog",
					icon = painterResource(Res.drawable.ic_explore_outlined),
					checked = current == SettingsPages.Catalog,
					onClick = { onOpenPage(SettingsPages.Catalog) }
				)
			}

			item("library") {
				SettingsDefaults.itemClickable(
					title = stringResource(Res.string.library),
					icon = painterResource(Res.drawable.ic_collections_bookmark_outlined),
					checked = current == SettingsPages.Library,
					onClick = { onOpenPage(SettingsPages.Library) }
				)
			}

			item("player") {
				SettingsDefaults.itemClickable(
					title = stringResource(Res.string.player),
					icon = painterResource(Res.drawable.ic_video_settings_outlined),
					checked = current == SettingsPages.Player,
					onClick = { onOpenPage(SettingsPages.Player) }
				)
			}

			item("extensions") {
				SettingsDefaults.itemClickable(
					title = stringResource(Res.string.extensions),
					icon = painterResource(Res.drawable.ic_extension_outlined),
					checked = current == SettingsPages.Extensions,
					onClick = { onOpenPage(SettingsPages.Extensions) }
				)
			}

			item("advanced") {
				SettingsDefaults.itemClickable(
					title = stringResource(Res.string.advanced),
					icon = painterResource(Res.drawable.ic_dev_outlined),
					checked = current == SettingsPages.Advanced,
					onClick = { onOpenPage(SettingsPages.Advanced) }
				)
			}

			item("about") {
				SettingsDefaults.itemClickable(
					title = stringResource(Res.string.about),
					icon = painterResource(Res.drawable.ic_info_outlined),
					checked = current == SettingsPages.About,
					onClick = { onOpenPage(SettingsPages.About) }
				)
			}
		}
	}
}