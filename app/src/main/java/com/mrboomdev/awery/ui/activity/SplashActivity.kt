package com.mrboomdev.awery.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.App.Companion.isTv
import com.mrboomdev.awery.app.AweryLifecycle.Companion.exitApp
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runDelayed
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.app.ExtensionsManager
import com.mrboomdev.awery.databinding.ScreenSplashBinding
import com.mrboomdev.awery.extensions.ExtensionsFactory
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.activity.settings.setup.SetupActivity
import com.mrboomdev.awery.ui.tv.TvMainActivity
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val USE_NEW_SOURCES = false

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
	private lateinit var binding: ScreenSplashBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()

		try {
			applyTheme()
		} catch(e: Exception) {
			Log.e(TAG, "Failed to apply an theme!", e)
		}

		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		binding = ScreenSplashBinding.inflate(layoutInflater).apply {
			root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))
			status.setText(R.string.checking_if_crash_occurred)
		}

		window.navigationBarColor = resolveAttrColor(android.R.attr.colorBackground)
		setContentView(binding.root)

		CrashHandler.showDialogIfCrashHappened(this) {
			binding.status.setText(R.string.checking_database)

			lifecycleScope.launch(Dispatchers.IO) {
				try {
					database.listDao.all
				} catch(e: IllegalStateException) {
					Log.e(TAG, "Database is corrupted!", e)

					CrashHandler.showDialog(
						context = this@SplashActivity,
						titleRes = R.string.database_corrupted,
						throwable = e,
						dismissCallback = ::exitApp)

					return@launch
				}

				if(AwerySettings.SETUP_VERSION_FINISHED.value < SetupActivity.SETUP_VERSION) {
					startActivity(SetupActivity::class)
					finish()
					return@launch
				}

				if(USE_NEW_SOURCES) {
					ExtensionsManager.init(this@SplashActivity).onEach {
						launch(Dispatchers.Main) {
							binding.status.text = getString(R.string.loading_extensions_n, it.progress, it.max)
						}
					}.onCompletion {
						// Tv version isn't done yet at 100%
						startActivity(if(isTv && BuildConfig.DEBUG) TvMainActivity::class else MainActivity::class)
						finish()
					}.catch {
						CrashHandler.showDialog(
							context = this@SplashActivity,
							title = "Failed to load an ExtensionsFactory",
							throwable = it,
							dismissCallback = ::exitApp)
					}.collect()

					return@launch
				}

				ExtensionsFactory.getInstance().addCallback(object : AsyncFuture.Callback<ExtensionsFactory?> {
					override fun onSuccess(result: ExtensionsFactory) {
						// Tv version isn't done yet at 100%
						startActivity(if(isTv && BuildConfig.DEBUG) TvMainActivity::class else MainActivity::class)
						finish()
					}

					override fun onFailure(t: Throwable) {
						Log.e(TAG, "Failed to load an ExtensionsFactory!", t)

						CrashHandler.showDialog(
							context = this@SplashActivity,
							title = "Failed to load an ExtensionsFactory",
							throwable = t,
							dismissCallback = ::exitApp)
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
			binding.status.setText(R.string.loading_extensions)
			return
		}

		var progress: Long = 0
		var total: Long = 0

		for(manager in factory.managers) {
			val managerProgress = manager.progress
			progress += managerProgress.progress
			total += managerProgress.max
		}

		binding.status.text = getString(R.string.loading_extensions_n, progress, total)
		runDelayed({ this.update() }, 100)
	}

	companion object {
		private const val TAG = "SplashActivity"
	}
}