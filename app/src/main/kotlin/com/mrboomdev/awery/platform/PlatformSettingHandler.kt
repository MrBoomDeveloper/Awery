package com.mrboomdev.awery.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.showLoadingWindow
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLocales
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.app.services.BackupService
import com.mrboomdev.awery.app.update.UpdatesManager
import com.mrboomdev.awery.app.update.UpdatesManager.showUpdateDialog
import com.mrboomdev.awery.data.Constants
import com.mrboomdev.awery.data.Constants.DIRECTORY_IMAGE_CACHE
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.mobile.screens.settings.AboutActivity
import com.mrboomdev.awery.ui.mobile.screens.setup.SetupActivity
import com.mrboomdev.awery.util.ContentType
import com.mrboomdev.awery.util.exceptions.explain
import com.mrboomdev.awery.util.extensions.activity
import com.mrboomdev.awery.util.extensions.hasPermission
import com.mrboomdev.awery.util.extensions.requestPermission
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.startActivityForResult
import com.mrboomdev.awery.util.extensions.startService
import com.mrboomdev.awery.util.extensions.toChooser
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcrash.XCrash
import java.io.File
import java.util.Calendar
import kotlin.concurrent.thread

object PlatformSettingHandler {
	private const val TAG = "PlatformSettingHandler"

	fun handlePlatformClick(context: Context, setting: PlatformSetting) {
		when(setting.key) {
			AwerySettings.TRY_CRASH_NATIVE.key ->
				XCrash.testNativeCrash(false)

			AwerySettings.TRY_CRASH_JAVA.key ->
				XCrash.testJavaCrash(false)

			AwerySettings.TRY_CRASH_NATIVE_ASYNC.key ->
				thread { XCrash.testNativeCrash(false) }

			AwerySettings.TRY_CRASH_JAVA_ASYNC.key ->
				thread { XCrash.testJavaCrash(false) }

			AwerySettings.ABOUT.key ->
				context.startActivity(AboutActivity::class)

			AwerySettings.START_ONBOARDING.key ->
				context.startActivity(SetupActivity::class)

			AwerySettings.UI_LANGUAGE.key ->
				AweryLocales.showPicker(context)

			AwerySettings.PLAYER_SYSTEM_SUBTITLES.key ->
				context.startActivity(action = Settings.ACTION_CAPTIONING_SETTINGS)

			AwerySettings.SETUP_THEME.key ->
				context.startActivity(SetupActivity::class, extras = mapOf(
					SetupActivity.EXTRA_STEP to SetupActivity.STEP_THEMING,
					SetupActivity.EXTRA_FINISH_ON_COMPLETE to true
				))

			AwerySettings.CLEAR_IMAGE_CACHE.key -> {
				File(context.cacheDir, DIRECTORY_IMAGE_CACHE).deleteRecursively()
				toast(R.string.cleared_successfully)
			}

			AwerySettings.CLEAR_WEBVIEW_CACHE.key -> {
				File(context.cacheDir, Constants.DIRECTORY_WEBVIEW_CACHE).deleteRecursively()
				toast(R.string.cleared_successfully)
			}

			AwerySettings.CLEAR_NET_CACHE.key -> {
				File(context.cacheDir, Constants.DIRECTORY_NET_CACHE).deleteRecursively()
				toast(R.string.cleared_successfully)
			}

			AwerySettings.BACKUP.key -> {
				val activity = requireNotNull(context.activity) {
					"This action can be called only from an activity!"
				}

				val fileName = buildString {
					val date = Calendar.getInstance()
					append("AweryBackup-[")

					append(date[Calendar.YEAR])
					append("-")
					append(date[Calendar.MONTH])
					append("-")
					append(date[Calendar.DAY_OF_MONTH])

					append("]-[")

					append(date[Calendar.HOUR_OF_DAY])
					append("-")
					append(date[Calendar.MINUTE])
					append("-")
					append(date[Calendar.SECOND])

					append("].awerybck")
				}

				if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !context.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					activity.requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, { didGrant ->
						if(didGrant) {
							handlePlatformClick(context, setting)
						} else {
							DialogBuilder(context).apply {
								setTitle("Permission required!")
								setMessage("Sorry, but you cannot create files without an storage permission.")
								setNegativeButton(R.string.dismiss) { dismiss() }

								setPositiveButton("Open settings") {
									context.startActivity(
										action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
										data = Uri.parse("package:${context.packageName}")
									)

									dismiss()
								}
							}.show()
						}
					})

					return
				}

				activity.startActivityForResult(
					action = Intent.ACTION_CREATE_DOCUMENT,
					type = ContentType.ANY.mimeType,
					categories = arrayOf(Intent.CATEGORY_OPENABLE),
					extras = mapOf(Intent.EXTRA_TITLE to fileName),
					callback = { resultCode, result ->
						if(resultCode != Activity.RESULT_OK) {
							return@startActivityForResult
						}

						context.startService(
							BackupService::class, BackupService.Args(
							BackupService.Action.BACKUP, result!!.data!!))
					})
			}

			AwerySettings.RESTORE.key -> {
				val activity = requireNotNull(context.activity) {
					"This action can be called only from an activity!"
				}

				if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !context.hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
					activity.requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, { didGrant ->
						if(didGrant) {
							handlePlatformClick(context, setting)
						} else {
							DialogBuilder(context).apply {
								setTitle("Permission required!")
								setMessage("Sorry, but you cannot select files without an storage permission.")
								setNegativeButton(R.string.dismiss) { dismiss() }

								setPositiveButton("Open settings") {
									context.startActivity(
										action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
										data = Uri.parse("package:${context.packageName}")
									)

									dismiss()
								}
							}.show()
						}
					})

					return
				}

				activity.startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
					type = ContentType.ANY.mimeType
				}.toChooser("Choose a backup file"), { resultCode, result ->
					if(resultCode != Activity.RESULT_OK) return@startActivityForResult

					context.startService(
						BackupService::class, BackupService.Args(
						BackupService.Action.RESTORE, result!!.data!!))
				})
			}

			AwerySettings.CHECK_APP_UPDATE.key -> {
				val window = showLoadingWindow()

				val activity = requireNotNull(context.activity) {
					"This action can be called only from an activity!"
				}

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
					showUpdateDialog(activity, update)
					window.dismiss()
				}
			}

			else -> throw NotImplementedError("Oops... It looks that you've forgot to implement an action for: ${setting.key}")
		}
	}
}