package com.mrboomdev.awery.ui.activity.settings

import android.app.Activity
import android.content.Intent
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
import com.mrboomdev.awery.app.CrashHandler.CrashReport
import com.mrboomdev.awery.app.data.Constants
import com.mrboomdev.awery.app.data.Constants.DIRECTORY_IMAGE_CACHE
import com.mrboomdev.awery.app.data.settings.SettingsItem
import com.mrboomdev.awery.app.services.BackupService
import com.mrboomdev.awery.app.update.UpdatesManager
import com.mrboomdev.awery.app.update.UpdatesManager.showUpdateDialog
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.activity.settings.setup.SetupActivity
import com.mrboomdev.awery.util.ContentType
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.startActivityForResult
import com.mrboomdev.awery.util.extensions.startService
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
			AwerySettings.EXPERIMENTS -> anyContext.startActivity(ExperimentsActivity::class)
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

				context.startActivityForResult(
					action = Intent.ACTION_CREATE_DOCUMENT,
					type = ContentType.ANY.mimeType,
					categories = arrayOf(Intent.CATEGORY_OPENABLE),
					extras = mapOf(Intent.EXTRA_TITLE to defaultName),
					callback = { resultCode, result ->
						if(resultCode != Activity.RESULT_OK) {
							return@startActivityForResult
						}

						context.startService<BackupService>(
							action = BackupService.ACTION_BACKUP,
							data = result!!.data)
					})
			}

			AwerySettings.RESTORE -> {
				val context = getAnyActivity<Activity>()!!

				context.startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
					setType(ContentType.ANY.mimeType)
				}, "Choose a backup file"), { resultCode, result ->
					if(resultCode != Activity.RESULT_OK) return@startActivityForResult

					context.startService<BackupService>(
						action = BackupService.ACTION_RESTORE,
						data = result!!.data)
				})
			}

			AwerySettings.CHECK_APP_UPDATE -> {
				val window = showLoadingWindow()

				CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
					Log.e(TAG, "Failed to check for updates!", t)
					window.dismiss()

					if(t is CancellationException) {
						toast(ExceptionDescriptor.getTitle(t, anyContext), 1)
						return@CoroutineExceptionHandler
					}

					CrashHandler.showErrorDialog(
						CrashReport.Builder()
							.setTitle("Failed to check for updates")
							.setPrefix(R.string.please_report_bug_app)
							.setThrowable(t)
							.build())
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