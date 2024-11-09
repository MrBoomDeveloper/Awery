package com.mrboomdev.awery.app.services

import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.showLoadingWindow
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.restartApp
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.util.io.unzipFiles
import com.mrboomdev.awery.util.io.zipFiles
import com.mrboomdev.safeargsnext.owner.SafeArgsService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "BackupService"
private val BACKUP_DIRECTORIES = arrayOf("shared_prefs", "databases")

class BackupService : SafeArgsService<BackupService.Args>() {
	class Args(val action: Action, val targetUri: Uri)

	enum class Action {
		BACKUP, RESTORE
	}

	override fun onStartCommand(args: Args?, flags: Int, startId: Int): Int {
		if(args == null) throw NullPointerException("Arguments are required!")

		when(args.action) {
			Action.BACKUP -> startBackup(args.targetUri)
			Action.RESTORE -> startRestore(args.targetUri)
		}

		return super.onStartCommand(args, flags, startId)
	}

	private fun startBackup(into: Uri) {
		val popup = showLoadingWindow()

		CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
			CoroutineScope(Dispatchers.Main).launch {
				Log.e(TAG, "Failed to create an backup", t)
				CrashHandler.showDialog(title = "Failed to create an backup", throwable = t)
				popup.dismiss()
				stopSelf()
			}
		}).launch {
			zipFiles(HashMap<String, File>().apply {
				for(dir in BACKUP_DIRECTORIES) {
					val list = File(dataDir, dir).listFiles() ?: continue

					for(file in list) {
						this[dir + "/" + file.name] = file
					}
				}
			}, into)

			toast(R.string.backup_success)
			runOnUiThread { popup.dismiss() }
			stopSelf()
		}
	}

	private fun startRestore(uri: Uri) {
		val window = showLoadingWindow()

		CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
			CoroutineScope(Dispatchers.Main).launch {
				Log.e(TAG, "Failed to restore an backup", t)
				CrashHandler.showDialog(title = "Failed to restore an backup", throwable = t)
				window.dismiss()
				stopSelf()
			}
		}).launch {
			for(dir in BACKUP_DIRECTORIES) {
				File(dataDir, dir).deleteRecursively()
			}

			unzipFiles(uri, dataDir)
			toast(R.string.restore_success)
			restartApp()
			stopSelf()
		}
	}

	override fun onBind(intent: Intent): IBinder? {
		return null
	}
}