package com.mrboomdev.awery.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mrboomdev.awery.app.App.getDatabase
import com.mrboomdev.awery.app.AweryLifecycle
import com.mrboomdev.awery.app.AweryLifecycle.runDelayed
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.app.CrashHandler.CrashReport
import com.mrboomdev.awery.databinding.ScreenSplashBinding
import com.mrboomdev.awery.extensions.ExtensionsFactory
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.activity.MainActivity
import com.mrboomdev.awery.ui.activity.settings.setup.SetupActivity
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
	private var binding: ScreenSplashBinding? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()

		try {
			applyTheme()
		} catch(e: Exception) {
			Log.e(TAG, "Failed to apply an theme!", e)
		}

		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		binding = ScreenSplashBinding.inflate(layoutInflater)
		binding!!.root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))
		window.navigationBarColor = resolveAttrColor(android.R.attr.colorBackground)
		setContentView(binding!!.root)

		binding!!.status.text = "Checking the database..."

		CrashHandler.reportIfCrashHappened(this) {
			lifecycleScope.launch(Dispatchers.IO) {
				try {
					getDatabase().listDao.all
				} catch(e: IllegalStateException) {
					Log.e(TAG, "Database is corrupted!", e)

					CrashHandler.showErrorDialog(this@SplashActivity, CrashReport.Builder()
							.setTitle("Database is corrupted!")
							.setThrowable(e)
							.setDismissCallback { AweryLifecycle.exitApp() }
							.build())

					return@launch
				}

				if(AwerySettings.SETUP_VERSION_FINISHED.value < SetupActivity.SETUP_VERSION) {
					startActivity(SetupActivity::class)
					finish()
					return@launch
				}

				ExtensionsFactory.getInstance().addCallback(object : AsyncFuture.Callback<ExtensionsFactory?> {
					override fun onSuccess(result: ExtensionsFactory) {
						startActivity(Intent(this@SplashActivity, MainActivity::class.java))
						finish()
					}

					override fun onFailure(t: Throwable) {
						Log.e(TAG, "Failed to load an ExtensionsFactory!", t)

						CrashHandler.showErrorDialog(
							this@SplashActivity, CrashReport.Builder()
								.setTitle("Failed to load an ExtensionsFactory")
								.setThrowable(t)
								.setDismissCallback { AweryLifecycle.exitApp() }
								.build())
					}
				})

				runOnUiThread { update() }
			}
		}
	}

	private fun update() {
		if(isDestroyed) return
		val factory = ExtensionsFactory.getInstanceNow()

		if(factory == null) {
			binding!!.status.text = "Loading extensions..."
			return
		}

		var progress: Long = 0
		var total: Long = 0

		for(manager in factory.managers) {
			val managerProgress = manager.progress
			progress += managerProgress.progress
			total += managerProgress.max
		}

		binding!!.status.text = "Loading extensions $progress/$total"
		runDelayed({ this.update() }, 100)
	}

	companion object {
		private const val TAG = "SplashActivity"
	}
}