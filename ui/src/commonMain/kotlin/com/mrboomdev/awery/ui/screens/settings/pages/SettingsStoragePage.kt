package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.mrboomdev.awery.core.utils.deleteRecursively
import com.mrboomdev.awery.core.utils.sizeRecursively
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_delete_outlined
import com.mrboomdev.awery.resources.storage
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemClickable
import com.mrboomdev.awery.ui.screens.settings.itemDialog
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.size
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsStoragePage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?,
	onOpenPage: (SettingsPages) -> Unit
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.storage)) }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding
		) { 
			item("clearCache") {
				var cacheSize by remember { mutableLongStateOf(-1L) }
				
				LaunchedEffect(Unit) {
					cacheSize = FileKit.cacheDir.sizeRecursively()
				}
				
				SettingsDefaults.itemDialog(
					icon = painterResource(Res.drawable.ic_delete_outlined),
					title = "Clear cache",
					description = if(cacheSize == -1L) {
						"Loading..."
					} else HumanReadable.fileSize(cacheSize, 2),
					dialog = {
						Dialog(onDismissRequest = {}) {
							CircularProgressIndicator()
						}

						LaunchedEffect(Unit) {
							FileKit.cacheDir.deleteRecursively()
							cacheSize = FileKit.cacheDir.sizeRecursively()
							dismiss()
						}
					}
				)
			}
		}
	}
}