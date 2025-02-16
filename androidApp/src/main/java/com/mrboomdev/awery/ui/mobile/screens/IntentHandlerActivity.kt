package com.mrboomdev.awery.ui.mobile.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.services.BackupService
import com.mrboomdev.awery.app.theme.ThemeManager.setThemedContent
import com.mrboomdev.awery.data.db.item.DBRepository
import com.mrboomdev.awery.data.settings.NicePreferences
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.sources.yomi.tachiyomi.TachiyomiManager
import com.mrboomdev.awery.ui.components.DEFAULT_DIALOG_PADDING
import com.mrboomdev.awery.ui.components.MaterialDialog
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity
import com.mrboomdev.awery.data.FileType
import com.mrboomdev.awery.platform.Platform.toast
import com.mrboomdev.awery.util.extensions.cleanUrl
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.utils.tryOr
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.fileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val TAG = "IntentHandlerActivity"

sealed interface Action {
	data class AddRepository(
		val type: Type,
		val url: String
	): Action {
		enum class Type(val managerId: String) {
			ANIYOMI(AniyomiManager.ID),
			TACHIYOMI(TachiyomiManager.ID),
			AWERY("TODO: WRITE HERE AN AWERY SOURCES MANAGER WHEN IT'LL BE DONE")
		}
	}
	
	data class StartActivity(val intent: Intent): Action
	data class InstallExtension(val file: Uri): Action
	data class RestoreBackup(val file: Uri): Action
	data object Undefined: Action
}

class IntentHandlerActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		tryOr({ installSplashScreen() }) { Log.e(TAG, "Failed to install an splash screen!", it) }
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		
		setThemedContent {
			var isLoading by remember { mutableStateOf(false) }
			val currentAction by remember { mutableStateOf(resolveAction(intent)) }
			
			LaunchedEffect(currentAction) { 
				when(val action = currentAction) {
					// These actions are being handled by using dialogs
					is Action.AddRepository, is Action.RestoreBackup, is Action.InstallExtension -> {}
					
					is Action.StartActivity -> {
						startActivity(action.intent)
						finish()
					}
					
					Action.Undefined -> {
						toast("This link isn't available in the current app version!", 1)
						finish()
					}
				}
			}
			
			Box(contentAlignment = Alignment.Center) {
				if(isLoading) {
					CircularProgressIndicator()
				}
			}
			
			if(!isLoading) {
				when(val action = currentAction) {
					// These actions are being handled in the LaunchedEffect
					is Action.StartActivity, Action.Undefined -> {}
					
					is Action.AddRepository -> {
						MaterialDialog(
							onDismissRequest = { finish() },
							modifier = Modifier.padding(horizontal = DEFAULT_DIALOG_PADDING),
							
							title = {
								Text(
									style = MaterialTheme.typography.headlineMedium,
									text = "Add an repository"
								)
							},
							
							dismissButton = {
								TextButton(onClick = this@MaterialDialog::requestDismiss) {
									Text(text = stringResource(Res.string.cancel))
								}
							},
							
							confirmButton = {
								TextButton(onClick = {
									isLoading = true
									
									lifecycleScope.launch(Dispatchers.IO) {
										val repos = database.repositoryDao.getRepositories(action.type.managerId)
										
										if(repos.find { it.url == action.url } != null) {
											toast("Repository already exists!", 1)
											finish()
											return@launch
										}
										
										database.repositoryDao.add(DBRepository(action.url, action.type.managerId))
										toast("Repository added successfully", 1)
										finish()
									}
								}) {
									Text(text = stringResource(Res.string.confirm))
								}
							}
						) {
							Text("Do really want to add an \"${action.url}\" repository?")
						}
					}
					
					is Action.InstallExtension -> TODO()
					
					is Action.RestoreBackup -> {
						MaterialDialog(
							onDismissRequest = { finish() },
							modifier = Modifier.padding(horizontal = DEFAULT_DIALOG_PADDING),
							
							title = {
								Text(
									style = MaterialTheme.typography.headlineMedium,
									text = stringResource(Res.string.restore_backup)
								)
							},
							
							dismissButton = {
								TextButton(onClick = this@MaterialDialog::requestDismiss) {
									Text(text = stringResource(Res.string.cancel))
								}
							},
							
							confirmButton = {
								TextButton(onClick = {
									isLoading = true
									startService(buildIntent(BackupService::class, BackupService.Args(
										BackupService.Action.RESTORE, action.file, action.file.fileType!!)))
								}) {
									Text(text = stringResource(Res.string.confirm))
								}
							}
						) {
							Text("Are you sure want to restore from this backup? All your current data will be erased!")
						}
					}
				}
			}
		}
	}
	
	private fun resolveAction(intent: Intent): Action {
		val uri = intent.data ?: return Action.Undefined
		return resolveUrlAction(uri) ?: resolveFileAction(uri) ?: Action.Undefined
	}
	
	private fun resolveUrlAction(uri: Uri) = when(uri.scheme) {
		"aniyomi" -> when(uri.host) {
			"add-repo" -> Action.AddRepository(
				type = Action.AddRepository.Type.ANIYOMI,
				url = requireNotNull(uri.getQueryParameter("url")?.cleanUrl()) {
					"Aniyomi repository url is null! $uri"
				}
			)
				
			else -> null
		}
			
		"tachiyomi" -> when(uri.host) {
			"add-repo" -> Action.AddRepository(
				type = Action.AddRepository.Type.TACHIYOMI,
				url = requireNotNull(uri.getQueryParameter("url")?.cleanUrl()) {
					"Tachiyomi repository url is null! $uri"
				}
			)
				
			else -> null
		}
			
		"awery" -> when(uri.host) {
			"add-repo" -> Action.AddRepository(
				type = Action.AddRepository.Type.AWERY,
				url = requireNotNull(uri.getQueryParameter("url")?.cleanUrl()) {
					"Awery repository url is null! $uri"
				}
			)
				
			// TODO: Use "new" settings when they'll be done
			"experiments" -> Action.StartActivity(SettingsActivity.createSettingsIntent(
				this, NicePreferences.getSettingsMap().findItem("experiments")))
				
			else -> null
		}
			
		else -> null
	}
	
	private fun resolveFileAction(uri: Uri) = when(uri.fileType) {
		FileType.DANTOTSU_BACKUP, FileType.YOMI_BACKUP, FileType.AWERY_BACKUP -> Action.RestoreBackup(uri)
		FileType.APK -> Action.InstallExtension(uri)
		null -> null
	}
	
	override fun finish() {
		// Clear an intent, so that action wouldn't be called twice
		intent = null
		super.finish()
	}
}