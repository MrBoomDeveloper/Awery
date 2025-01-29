package com.mrboomdev.awery.ui.mobile.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigatorSaver
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.FadeTransition
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runDelayed
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.sources.ExtensionsManager
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.databinding.ScreenSplashBinding
import com.mrboomdev.awery.extensions.ExtensionsFactory
import com.mrboomdev.awery.MainActivity
import com.mrboomdev.awery.app.theme.LocalAweryTheme
import com.mrboomdev.awery.app.theme.ThemeManager.setThemedContent
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.LocalSettingHandler
import com.mrboomdev.awery.platform.PlatformSettingHandler
import com.mrboomdev.awery.platform.SettingHandler
import com.mrboomdev.awery.platform.android.AndroidGlobals.exitApp
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.mobile.screens.setup.SetupActivity
import com.mrboomdev.awery.ui.routes.SettingsRoute
import com.mrboomdev.awery.ui.routes.SplashRoute
import com.mrboomdev.awery.ui.tv.TvMainActivity
import com.mrboomdev.awery.ui.utils.KSerializerNavigatorSaver
import com.mrboomdev.awery.ui.utils.LocalToaster
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.tryOr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val TAG = "SplashActivity"
const val SPLASH_EXTRA_BOOLEAN_ENABLE_COMPOSE = "enable_compose"
const val SPLASH_EXTRA_BOOLEAN_REDIRECT_SETTINGS = "redirect_settings"

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
	private lateinit var binding: ScreenSplashBinding
	
	@OptIn(ExperimentalSerializationApi::class, ExperimentalResourceApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		
		// TODO: Remove after Compose migration finished
		if(!AwerySettings.EXPERIMENT_COMPOSE_UI.value) {
			tryOr({ applyTheme() }) { Log.e(TAG, "Failed to apply an theme!", it) }
		}
		
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		
		if(AwerySettings.EXPERIMENT_COMPOSE_UI.value || intent.getBooleanExtra(SPLASH_EXTRA_BOOLEAN_ENABLE_COMPOSE, false)) {
			val initialRoute = when {
				intent.getBooleanExtra(SPLASH_EXTRA_BOOLEAN_REDIRECT_SETTINGS, false) -> {
					val settings = runBlocking {
						Res.readBytes("files/app_settings.json").toString(Charsets.UTF_8)
					}.let {
						@Suppress("JSON_FORMAT_REDUNDANT")
						Json {
							decodeEnumsCaseInsensitive = true
							isLenient = true
						}.decodeFromString<PlatformSetting>(it).apply { restoreValues() }
					}
					
					@Suppress("JSON_FORMAT_REDUNDANT")
					SettingsRoute(settings)
				}
				
				else -> SplashRoute()
			}
			
			setThemedContent {
				@OptIn(ExperimentalVoyagerApi::class)
				CompositionLocalProvider(LocalNavigatorSaver provides KSerializerNavigatorSaver()) {
					Navigator(
						screen = initialRoute,
						disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false)
					) { navigator ->
						val toasterState = rememberToasterState()
						
						CompositionLocalProvider(LocalToaster provides toasterState) {
							CompositionLocalProvider(LocalSettingHandler provides object : SettingHandler {
								override fun openScreen(screen: Setting) {
									navigator.push(SettingsRoute(screen))
								}
								
								override fun handleClick(setting: Setting) {
									if(setting is PlatformSetting) {
										PlatformSettingHandler.handlePlatformClick(this@SplashActivity, setting)
									} else {
										throw UnsupportedOperationException("${setting::class.qualifiedName} click handle isn't supported!")
									}
								}
							}) {
								@OptIn(ExperimentalVoyagerApi::class)
								FadeTransition(
									navigator = navigator,
									disposeScreenAfterTransitionEnd = true
								)
							}
						}
						
						Toaster(
							state = toasterState,
							darkTheme = LocalAweryTheme.current.isDark
						)
					}
				}
			}
			
			return
		}

		binding = ScreenSplashBinding.inflate(layoutInflater).apply {
			root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))
			status.text = i18n(Res.string.checking_if_crash_occurred)
		}

		window.navigationBarColor = resolveAttrColor(android.R.attr.colorBackground)
		setContentView(binding.root)

		CrashHandler.showDialogIfCrashHappened(this) {
			binding.status.text = i18n(Res.string.checking_database)

			lifecycleScope.launch(Dispatchers.IO) {
				try {
					database.listDao.all
				} catch(e: IllegalStateException) {
					Log.e(TAG, "Database is corrupted!", e)

					CrashHandler.showDialog(
						context = this@SplashActivity,
						title = i18n(Res.string.database_corrupted),
						throwable = e,
						dismissCallback = ::exitApp)

					return@launch
				}
				
				@Suppress("ControlFlowWithEmptyBody")
				while(!App.didInit) {}
				
				if(AwerySettings.SETUP_VERSION_FINISHED.value < SetupActivity.SETUP_VERSION) {
					startActivity(buildIntent(SetupActivity::class))
					finish()
					return@launch
				}

				if(AwerySettings.EXPERIMENT_SPLASH_LOAD_SOURCES.value) {
					try {
						ExtensionsManager.init(applicationContext).data.onEach {
							launch(Dispatchers.Main) {
								binding.status.text = i18n(Res.string.loading_extensions_n, it.value, it.max)
							}
						}.collect()
					} catch(t: Throwable) {
						Log.e(TAG, "Extensions loading failed!", t)

						CrashHandler.showDialog(
							context = this@SplashActivity,
							title = "Extensions loading failed",
							throwable = t,
							dismissCallback = ::exitApp)

						return@launch
					}

					startActivity(buildIntent(if(/*isTv*/AwerySettings.EXPERIMENT_TV_COMPOSE.value) TvMainActivity::class else MainActivity::class))
					finish()

					return@launch
				}

				ExtensionsFactory.getInstance().addCallback(object : AsyncFuture.Callback<ExtensionsFactory?> {
					override fun onSuccess(result: ExtensionsFactory) {
						startActivity(buildIntent(if(/*isTv*/AwerySettings.EXPERIMENT_TV_COMPOSE.value) TvMainActivity::class else MainActivity::class))
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
			binding.status.text = i18n(Res.string.loading_extensions)
			return
		}

		var progress = 0L
		var total = 0L

		for(manager in factory.managers) {
			val managerProgress = manager.progress
			progress += managerProgress.value
			total += managerProgress.max
		}

		binding.status.text = i18n(Res.string.loading_extensions_n, progress, total)
		runDelayed({ this.update() }, 100)
	}
}