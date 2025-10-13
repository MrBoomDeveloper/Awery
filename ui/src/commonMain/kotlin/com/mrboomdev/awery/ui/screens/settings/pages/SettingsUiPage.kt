package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.components.AlertDialog
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemDialog
import com.mrboomdev.awery.ui.screens.settings.itemSetting
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsUiPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.ui)) }
	) { contentPadding ->
		val libraryStyle by AwerySettings.libraryStyle.collectAsState()
		val libraryDefaultList by AwerySettings.libraryDefaultTab.collectAsState()
		val libraryLists by Awery.database.lists.observeAll().collectAsState(emptyList())
		
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding
		) {
			item("startPage") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.mainDefaultTab,
					icon = painterResource(Res.drawable.ic_home_outlined),
					title = "Start page",
					enumValues = {
						when(it) {
							AwerySettings.MainTab.HOME -> stringResource(Res.string.home)
							AwerySettings.MainTab.SEARCH -> stringResource(Res.string.search)
							AwerySettings.MainTab.NOTIFICATIONS -> stringResource(Res.string.notifications)
							AwerySettings.MainTab.LIBRARY -> stringResource(Res.string.library)
						}
					}
				)
			}

			item("libraryStyle") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.libraryStyle,
					title = "Library style",
					enumValues = {
						when(it) {
							AwerySettings.LibraryStyle.TABBED -> "Tabbed"
							AwerySettings.LibraryStyle.COLUMN -> "Column"
						}
					}
				)
			}
			
			if(libraryStyle == AwerySettings.LibraryStyle.TABBED) {
				item("libraryStartPage") {
					SettingsDefaults.itemDialog(
						title = "Library default page",
						content = {
							libraryLists.firstOrNull { it.id == libraryDefaultList }?.also { 
								Text(
									color = MaterialTheme.colorScheme.primary,
									text = it.name
								)
							}
						}
					) {
						val coroutineScope = rememberCoroutineScope()
						var isLoading by remember { mutableStateOf(false) }
						var selected by remember { mutableLongStateOf(AwerySettings.libraryDefaultTab.value) }
						
						AlertDialog(
							onDismissRequest = ::dismiss,
							title = { Text("Library default page") },
							
							confirmButton = {
								TextButton(
									enabled = !isLoading,
									onClick = {
										coroutineScope.launch {
											isLoading = true
											AwerySettings.libraryDefaultTab.set(selected)
											dismiss()
										}
									}
								) {
									Text(stringResource(Res.string.confirm))
								}
							},
							
							cancelButton = {
								TextButton(onClick = ::dismiss) {
									Text(stringResource(Res.string.cancel))
								}
							}
						) {
							LazyColumn(Modifier.fillMaxWidth()) {
								items(
									items = libraryLists,
									key = { it.id }
								) { list ->
									val interactionSource = remember { MutableInteractionSource() }

									Row(
										modifier = Modifier
											.clickable(
												interactionSource = interactionSource,
												indication = LocalIndication.current,
												role = Role.RadioButton,
												onClick = { selected = list.id }
											)
											.fillMaxWidth()
											.padding(vertical = 1.dp),
										horizontalArrangement = Arrangement.spacedBy(8.dp),
										verticalAlignment = Alignment.CenterVertically
									) {
										Text(list.name)
										Spacer(Modifier.weight(1f))
										RadioButton(
											interactionSource = interactionSource,
											selected = list.id == selected,
											onClick = { selected = list.id }
										)
									}
								}
							}
						}
					}
				}
			}

			item("navigationLabels") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.showNavigationLabels,
					title = "Show navigation labels",
					enumValues = {
						when(it) {
							AwerySettings.NavigationLabels.SHOW -> "Always show"
							AwerySettings.NavigationLabels.ACTIVE -> "Only active"
							AwerySettings.NavigationLabels.HIDE -> "Don't show"
						}
					}
				)
			}
		}
	}
}