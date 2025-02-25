package com.mrboomdev.awery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.CrashHandler
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.platform.didInit
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.sources.ExtensionsManager
import com.mrboomdev.awery.ui.components.MaterialDialog
import com.mrboomdev.awery.ui.routes.MainRoute
import com.mrboomdev.awery.utils.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.io.File

private sealed interface LoadingStatus {
	data object InitializingApp: LoadingStatus
	data object CheckingCrashes: LoadingStatus
	data object CheckingDatabase: LoadingStatus
	data class LoadingExtensions(val progress: Progress?): LoadingStatus
	data object Finished: LoadingStatus
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
	modifier: Modifier = Modifier
) {
	var loadingStatus by remember { mutableStateOf<LoadingStatus>(LoadingStatus.InitializingApp) }
	val crashLogs = remember { mutableStateListOf<Pair<File, String>>() }
	var share by remember { mutableStateOf(false) }
	val coroutineScope = rememberCoroutineScope()
	val navigator = LocalNavigator.currentOrThrow
	
	LaunchedEffect(loadingStatus) {
		when(val status = loadingStatus) {
			LoadingStatus.InitializingApp -> {
				if(Platform.didInit) {
					loadingStatus = LoadingStatus.CheckingCrashes
					return@LaunchedEffect
				}
				
				coroutineScope.launch {
					await { Platform.didInit }
					loadingStatus = LoadingStatus.CheckingCrashes
				}
			}
			
			LoadingStatus.CheckingCrashes -> {
				val crashes = CrashHandler.crashLogs
				
				if(crashes.isEmpty()) {
					loadingStatus = LoadingStatus.CheckingDatabase
				} else {
					crashLogs += crashes.map { 
						it to it.readText()
					}.toMutableList()
				}
			}
			
			LoadingStatus.CheckingDatabase -> {
				loadingStatus = LoadingStatus.LoadingExtensions(null)
			}
			
			is LoadingStatus.LoadingExtensions -> {
				if(status.progress != null) return@LaunchedEffect
				
				coroutineScope.launch(Dispatchers.Default) {
					ExtensionsManager.init().data.onEach {
						// Compose doesn't want to rerender because we do use the same Progress instance,
						// so to fix that problem we do copy it. Not the best memory management. ik.
						loadingStatus = LoadingStatus.LoadingExtensions(it.copy())
					}.onCompletion {
						loadingStatus = LoadingStatus.Finished
					}.buffer(10).collect()
				}
			}
			
			LoadingStatus.Finished -> {
				navigator.replace(MainRoute())
			}
		}
	}
	
	if(crashLogs.isNotEmpty()) {
		MaterialDialog(
			modifier = Modifier.padding(horizontal = 8.dp),
			title = { Text(i18n(Res.string.app_crash) + " #${crashLogs.size}") },
			
			content = {
				SelectionContainer {
					Text(
						modifier = Modifier.verticalScroll(rememberScrollState()),
						text = crashLogs.last().second
					)
				}
			},
			
			confirmButton = {
				TextButton(onClick = ::requestDismiss) {
					Text(text = stringResource(Res.string.continue_action))
				}
			},
			
			onDismissRequest = {
				crashLogs.last().first.delete()
				crashLogs.removeLast()
				
				if(crashLogs.isEmpty()) {
					loadingStatus = LoadingStatus.CheckingDatabase
				}
			}
		)
	}
	
	if(share) {
		ModalBottomSheet(
			onDismissRequest = { share = false },
			content = {
				
			}
		)
	}
		
	Column(
		modifier = modifier.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		CircularProgressIndicator(
			strokeWidth = 2.75.dp,
			modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
		)
			
		Text(
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.primary,
			text = when(val status = loadingStatus) {
				LoadingStatus.InitializingApp -> "Initializing app"
				
				LoadingStatus.CheckingCrashes -> stringResource(
					Res.string.checking_if_crash_occurred)
					
				LoadingStatus.CheckingDatabase -> stringResource(
					Res.string.checking_database)
					
				is LoadingStatus.LoadingExtensions -> {
					if(status.progress == null) {
						stringResource(Res.string.loading_extensions)
					} else {
						stringResource(
							Res.string.loading_extensions_n,
							status.progress.value,
							status.progress.max
						)
					}
				}
				
				LoadingStatus.Finished -> ""
			}
		)
	}
}