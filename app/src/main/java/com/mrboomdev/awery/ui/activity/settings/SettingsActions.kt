package com.mrboomdev.awery.ui.activity.settings

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.showLoadingWindow
import com.mrboomdev.awery.app.App.toast
import com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity
import com.mrboomdev.awery.app.AweryLifecycle.getAnyContext
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.app.CrashHandler.CrashReport
import com.mrboomdev.awery.app.services.BackupService
import com.mrboomdev.awery.app.update.UpdatesManager
import com.mrboomdev.awery.app.update.UpdatesManager.showUpdateDialog
import com.mrboomdev.awery.data.Constants
import com.mrboomdev.awery.data.Constants.DIRECTORY_IMAGE_CACHE
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.activity.settings.setup.SetupActivity
import com.mrboomdev.awery.util.ContentType
import com.mrboomdev.awery.util.exceptions.CancelledException
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.startActivityForResult
import com.mrboomdev.awery.util.extensions.startService
import com.mrboomdev.awery.util.io.FileUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Contract
import xcrash.XCrash
import java.io.File
import java.util.Calendar
import kotlin.concurrent.thread

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

			AwerySettings.ABOUT -> getAnyContext().startActivity(AboutActivity::class)
			AwerySettings.START_ONBOARDING -> getAnyContext().startActivity(SetupActivity::class)
			AwerySettings.EXPERIMENTS -> getAnyContext().startActivity(ExperimentsActivity::class)

			AwerySettings.PLAYER_SYSTEM_SUBTITLES -> getAnyContext().startActivity(
				action = Settings.ACTION_CAPTIONING_SETTINGS)

			AwerySettings.SETUP_THEME -> getAnyContext().startActivity(
				SetupActivity::class, extras = mapOf(
					SetupActivity.EXTRA_STEP to SetupActivity.STEP_THEMING,
					SetupActivity.EXTRA_FINISH_ON_COMPLETE to true
				))

			AwerySettings.CLEAR_IMAGE_CACHE -> {
				FileUtil.deleteFile(File(getAnyContext().cacheDir, DIRECTORY_IMAGE_CACHE))
				toast(R.string.cleared_successfully)
			}

			AwerySettings.CLEAR_WEBVIEW_CACHE -> {
				FileUtil.deleteFile(File(getAnyContext().cacheDir, Constants.DIRECTORY_WEBVIEW_CACHE))
				toast(R.string.cleared_successfully)
			}

			AwerySettings.CLEAR_NET_CACHE -> {
				FileUtil.deleteFile(File(getAnyContext().cacheDir, Constants.DIRECTORY_NET_CACHE))
				toast(R.string.cleared_successfully)
			}

			AwerySettings.BACKUP -> {
				val date = Calendar.getInstance()

				val defaultName = "awery_backup_[" + date[Calendar.YEAR] + "_" +
						date[Calendar.MONTH] + "_" +
						date[Calendar.DATE] + "]_[" +
						date[Calendar.HOUR_OF_DAY] + "_" +
						date[Calendar.MINUTE] + "].awerybck"

				val context = getAnyActivity(Activity::class.java)!!

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
							data = result.data)
					})
			}

			AwerySettings.RESTORE -> {
				val context = getAnyActivity(Activity::class.java)!!

				context.startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
					setType(ContentType.ANY.mimeType)
				}, "Choose a backup file"), { resultCode, result ->
					if(resultCode != Activity.RESULT_OK) return@startActivityForResult

					context.startService<BackupService>(
						action = BackupService.ACTION_RESTORE,
						data = result.data)
				})
			}

			AwerySettings.CHECK_APP_UPDATE -> {
				val window = showLoadingWindow()

				CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
					Log.e(TAG, "Failed to check for updates!", t)
					window.dismiss()

					if(t is CancelledException) {
						toast(ExceptionDescriptor.getTitle(t, getAnyContext()), 1)
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
					showUpdateDialog(getAnyActivity(Activity::class.java)!!, update)
					window.dismiss()
				}
			}

			else -> toast("Unknown action: $actionName")
		}
	}
}