package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_bookmarks_outlined
import com.mrboomdev.awery.resources.library
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.utils.add
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsLibraryPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?,
	onOpenPage: (SettingsPages) -> Unit
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.library)) }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding.add(bottom = 16.dp)
		) { 
			item("lists") {
				SettingsDefaults.itemClickable(
					icon = painterResource(Res.drawable.ic_bookmarks_outlined),
					title = "Lists",
					onClick = { onOpenPage(SettingsPages.Lists) }
				)
			}
		}
	}
}