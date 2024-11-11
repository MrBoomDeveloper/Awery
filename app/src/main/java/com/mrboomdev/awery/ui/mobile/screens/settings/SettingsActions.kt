package com.mrboomdev.awery.ui.mobile.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.showLoadingWindow
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLocales
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.app.data.Constants
import com.mrboomdev.awery.app.data.Constants.DIRECTORY_IMAGE_CACHE
import com.mrboomdev.awery.app.data.settings.SettingsItem
import com.mrboomdev.awery.app.services.BackupService
import com.mrboomdev.awery.app.update.UpdatesManager
import com.mrboomdev.awery.app.update.UpdatesManager.showUpdateDialog
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.mobile.screens.setup.SetupActivity
import com.mrboomdev.awery.util.ContentType
import com.mrboomdev.awery.util.exceptions.explain
import com.mrboomdev.awery.util.extensions.hasPermission
import com.mrboomdev.awery.util.extensions.requestPermission
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.startActivityForResult
import com.mrboomdev.awery.util.extensions.startService
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Contract
import xcrash.XCrash
import java.io.File
import java.util.Calendar
import kotlin.concurrent.thread
import kotlin.coroutines.cancellation.CancellationException

object SettingsActions {
	private const val TAG = "SettingsActions"

	@JvmStatic
	@Contract(pure = true)
	fun run(item: SettingsItem) {
		val actionName = item.key ?: return

		when(actionName) {
			AwerySettings.TRY_CRASH_NATIVE -> XCrash.testNativeCrash(false)
			AwerySettings.TRY_CRASH_JAVA -> XCrash.testJavaCrash(false)
			AwerySettings.TRY_CRASH_NATIVE_ASYNC -> thread { XCrash.testNativeCrash(false) }
			AwerySettings.TRY_CRASH_JAVA_ASYNC -> thread { XCrash.testJavaCrash(false) }

			AwerySettings.ABOUT -> anyContext.startActivity(AboutActivity::class)
			AwerySettings.START_ONBOARDING -> anyContext.startActivity(SetupActivity::class)
			AwerySettings.UI_LANGUAGE -> AweryLocales.showPicker(getAnyActivity()!!)

			AwerySettings.PLAYER_SYSTEM_SUBTITLES -> anyContext.startActivity(
				action = Settings.ACTION_CAPTIONING_SETTINGS)

			AwerySettings.SETUP_THEME -> anyContext.startActivity(
				SetupActivity::class, extras = mapOf(
					SetupActivity.EXTRA_STEP to SetupActivity.STEP_THEMING,
					SetupActivity.EXTRA_FINISH_ON_COMPLETE to true
				))

			AwerySettings.CLEAR_IMAGE_CACHE -> {
				File(anyContext.cacheDir, DIRECTORY_IMAGE_CACHE).deleteRecursively()
				toast(R.string.cleared_successfully)
			}

			AwerySettings.CLEAR_WEBVIEW_CACHE -> {
				File(anyContext.cacheDir, Constants.DIRECTORY_WEBVIEW_CACHE).deleteRecursively()
				toast(R.string.cleared_successfully)
			}

			AwerySettings.CLEAR_NET_CACHE -> {
				File(appContext.cacheDir, Constants.DIRECTORY_NET_CACHE).deleteRecursively()
				toast(R.string.cleared_successfully)
			}

			AwerySettings.BACKUP -> {
				val date = Calendar.getInstance()

				val defaultName = "awery_backup_[" + date[Calendar.YEAR] + "_" +
						date[Calendar.MONTH] + "_" +
						date[Calendar.DATE] + "]_[" +
						date[Calendar.HOUR_OF_DAY] + "_" +
						date[Calendar.MINUTE] + "].awerybck"

				val context = getAnyActivity<Activity>()!!

				if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !context.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					context.requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, { didGrant ->
						if(didGrant) run(item) else (DialogBuilder(context).apply {
							setTitle("Permission required!")
							setMessage("Sorry, but you cannot create files without an storage permission.")
							setNegativeButton(R.string.dismiss) { dismiss() }

							setPositiveButton("Open settings") {
								context.startActivity(action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
									data = Uri.parse("package:${context.packageName}"))

								dismiss()
							}
						}).show()
					})

					return
				}

				context.startActivityForResult(
					action = Intent.ACTION_CREATE_DOCUMENT,
					type = ContentType.ANY.mimeType,
					categories = arrayOf(Intent.CATEGORY_OPENABLE),
					extras = mapOf(Intent.EXTRA_TITLE to defaultName),
					callback = { resultCode, result ->
						if(resultCode != Activity.RESULT_OK) {
							return@startActivityForResult
						}

						context.startService(BackupService::class, BackupService.Args(
							BackupService.Action.BACKUP, result!!.data!!))
					})
			}

			AwerySettings.RESTORE -> {
				val context = getAnyActivity<Activity>()!!

				if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !context.hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
					context.requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, { didGrant ->
						if(didGrant) run(item) else (DialogBuilder(context).apply {
							setTitle("Permission required!")
							setMessage("Sorry, but you cannot select files without an storage permission.")
							setNegativeButton(R.string.dismiss) { dismiss() }

							setPositiveButton("Open settings") {
								context.startActivity(action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
									data = Uri.parse("package:${context.packageName}"))

								dismiss()
							}
						}).show()
					})

					return
				}

				context.startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
					setType(ContentType.ANY.mimeType)
				}, "Choose a backup file"), { resultCode, result ->
					if(resultCode != Activity.RESULT_OK) return@startActivityForResult

					context.startService(BackupService::class, BackupService.Args(
						BackupService.Action.RESTORE, result!!.data!!))
				})
			}

			AwerySettings.CHECK_APP_UPDATE -> {
				val window = showLoadingWindow()

				CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
					Log.e(TAG, "Failed to check for updates!", t)
					window.dismiss()

					if(t is CancellationException) {
						toast(t.explain().title)
						return@CoroutineExceptionHandler
					}

					CrashHandler.showDialog(
						title = "Failed to check for updates",
						messagePrefixRes = R.string.please_report_bug_app,
						throwable = t)
				}).launch {
					val update = UpdatesManager.fetchLatestAppUpdate()
					showUpdateDialog(getAnyActivity<Activity>()!!, update)
					window.dismiss()
				}
			}

			else -> toast("Unknown action: $actionName")
		}
	}
}