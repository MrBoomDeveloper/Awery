package com.mrboomdev.awery.ui.mobile.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.App.Companion.showLoadingWindow
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getActivities
import com.mrboomdev.awery.app.data.db.item.DBRepository
import com.mrboomdev.awery.app.services.BackupService
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.cleanUrl
import com.mrboomdev.awery.util.extensions.startService
import com.mrboomdev.awery.util.io.FileUtil.fileName
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntentHandlerActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		try {
			installSplashScreen()
		} catch(e: Exception) {
			Log.e(TAG, "Failed to install an splash screen!", e)
		}

		applyTheme()
		super.onCreate(savedInstanceState)

		val uri = intent.data ?: run {
			return exit("No data was passed!")
		}

		if(uri.scheme != null && uri.scheme == "aniyomi") {
			if(uri.host != null && uri.host == "add-repo") {
				val loadingWindow = showLoadingWindow()
				val repo = uri.getQueryParameter("url")?.cleanUrl() ?: run {
					return exit("No url was specified!")
				}

				lifecycleScope.launch(Dispatchers.IO) {
					val repos = database.repositoryDao.getRepositories(AniyomiManager.MANAGER_ID)

					if(repos.find { it?.url == repo } != null) {
						loadingWindow.dismiss()
						return@launch exit("Repository already exists!")
					}

					val dbRepo = DBRepository(repo, AniyomiManager.MANAGER_ID)
					database.repositoryDao.add(dbRepo)

					loadingWindow.dismiss()
					exit("Repository added successfully")
				}
			} else {
				finish()
			}
		} else if(uri.path != null && uri.path!!.startsWith("/awery/app-login/")) {
			for(activity in getActivities<LoginActivity>()) {
				activity.completionUrl = uri.toString()
			}

			val intent = Intent(this, LoginActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
			startActivity(intent)
			finish()
		} else if(uri.fileName.let { it != null && it.endsWith(".awerybck") }) {
			DialogBuilder(this)
				.setTitle(R.string.restore_backup)
				.setMessage("Are you sure want to restore an saved backup? All your current data will be erased!")
				.setCancelable(false)
				.setNegativeButton(R.string.cancel) { finish() }
				.setPositiveButton(R.string.confirm) { dialog ->
					startService(BackupService::class, BackupService.Args(
						BackupService.Action.RESTORE, uri))

					dialog.dismiss()
				}.show()
		} else {
			exit("Unknown action!")
		}
	}

	private fun exit(message: String) {
		toast(message, 1)
		finish()
	}

	companion object {
		private const val TAG = "IntentHandlerActivity"
	}
}