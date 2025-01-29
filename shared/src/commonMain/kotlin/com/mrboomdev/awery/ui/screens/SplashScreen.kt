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
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.components.MaterialDialog
import com.mrboomdev.awery.ui.routes.MainRoute
import org.jetbrains.compose.resources.stringResource
import java.io.File

private sealed interface LoadingStatus {
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
	var loadingStatus by remember { mutableStateOf<LoadingStatus>(LoadingStatus.CheckingCrashes) }
	val crashLogs = remember { mutableStateListOf<Pair<File, String>>() }
	var share by remember { mutableStateOf(false) }
	val navigator = LocalNavigator.currentOrThrow
		
	LaunchedEffect(loadingStatus) {
		when(loadingStatus) {
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
				// TODO: Load extensions
				loadingStatus = LoadingStatus.Finished
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
		modifier = modifier,
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
				
				LoadingStatus.Finished -> stringResource(Res.string.status_finished)
			} + "\nWARNING! EVERYTHING IS STILL IN EARLY STAGES!"
		)
	}
}