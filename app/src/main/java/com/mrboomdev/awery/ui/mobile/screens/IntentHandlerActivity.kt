package com.mrboomdev.awery.ui.mobile.screens

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.App.Companion.showLoadingWindow
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.data.db.item.DBRepository
import com.mrboomdev.awery.app.data.settings.NicePreferences
import com.mrboomdev.awery.app.services.BackupService
import com.mrboomdev.awery.extensions.support.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity
import com.mrboomdev.awery.util.FileType
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.cleanUrl
import com.mrboomdev.awery.util.extensions.startService
import com.mrboomdev.awery.util.io.FileUtil.fileName
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.Throws

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

		try {
			handlePath(uri)
		} catch(e: UnsupportedOperationException) {
			try {
				if(!handleFile(uri)) {
					Log.e(TAG, "Unsupported path!", e)
					exit("Unsupported file!")
				}
			} catch(e: UnsupportedOperationException) {
				Log.e(TAG, "Unsupported file!", e)
				exit("Unsupported action!")
			}
		}
	}

	/**
	 * @return true if file was handled and false if no file type was passed.
	 * @throws UnsupportedOperationException if no file types were matched.
	 */
	@Throws(UnsupportedOperationException::class)
	private fun handleFile(uri: Uri): Boolean {
		return uri.fileName?.let {
			when(FileType.test(it)) {
				FileType.DANTOTSU_BACKUP -> {
					(DialogBuilder(this).apply {
						setTitle(R.string.restore_backup)
						setMessage("Are you sure want to restore an backup from Dantotsu? All your current data will be erased!")
						setCancelable(false)
						setNegativeButton(R.string.cancel) { finish() }

						setPositiveButton(R.string.confirm) {
							startService(BackupService::class, BackupService.Args(
								BackupService.Action.RESTORE, uri, FileType.DANTOTSU_BACKUP))

							dialog.dismiss()
						}
					}).show()

					return true
				}

				FileType.YOMI_BACKUP -> {
					(DialogBuilder(this).apply {
						setTitle(R.string.restore_backup)
						setMessage("Are you sure want to restore an backup from Tachiyomi? All your current data will be erased!")
						setCancelable(false)
						setNegativeButton(R.string.cancel) { finish() }

						setPositiveButton(R.string.confirm) {
							startService(BackupService::class, BackupService.Args(
								BackupService.Action.RESTORE, uri, FileType.YOMI_BACKUP))

							dialog.dismiss()
						}
					}).show()

					return true
				}

				FileType.APK -> {
					exit("APK installation isn't supported currently!")
					return true
				}

				FileType.AWERY_BACKUP -> {
					(DialogBuilder(this).apply {
						setTitle(R.string.restore_backup)
						setMessage("Are you sure want to restore an saved backup? All your current data will be erased!")
						setCancelable(false)
						setNegativeButton(R.string.cancel) { finish() }

						setPositiveButton(R.string.confirm) {
							startService(BackupService::class, BackupService.Args(
								BackupService.Action.RESTORE, uri))

							dialog.dismiss()
						}
					}).show()

					return true
				}

				else -> throw UnsupportedOperationException("")
			}
		} ?: false
	}

	@Throws(UnsupportedOperationException::class)
	private fun handlePath(uri: Uri) {
		when(uri.scheme) {
			"aniyomi" -> {
				when(uri.host) {
					"add-repo" -> {
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
					}

					else -> throw UnsupportedOperationException("Unsupported action (Unknown aniyomi path)")
				}
			}

			"awery" -> {
				when(uri.host) {
					"experiments" -> {
						SettingsActivity.start(this, NicePreferences.getSettingsMap().findItem("experiments"))
						finish()
					}

					else -> throw UnsupportedOperationException("Unsupported action (Unknown awery path)")
				}
			}

			else -> throw UnsupportedOperationException("Unsupported action (Unknown scheme)")
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