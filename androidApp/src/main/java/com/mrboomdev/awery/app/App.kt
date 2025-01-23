package com.mrboomdev.awery.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.window.OnBackInvokedCallback
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.room.Room.databaseBuilder
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideCustomImageLoader
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.resources.MaterialAttributes
import com.google.android.material.snackbar.Snackbar
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.app.AweryNotifications.registerNotificationChannels
import com.mrboomdev.awery.app.theme.ThemeManager
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.app.update.UpdatesChannel
import com.mrboomdev.awery.data.Constants
import com.mrboomdev.awery.data.db.AweryDB
import com.mrboomdev.awery.data.db.item.DBCatalogList
import com.mrboomdev.awery.extensions.data.CatalogList
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.CrashHandler
import com.mrboomdev.awery.platform.android.AndroidGlobals
import com.mrboomdev.awery.platform.android.AndroidGlobals.isTv
import com.mrboomdev.awery.platform.android.AndroidGlobals.toast
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.mobile.screens.BrowserActivity
import com.mrboomdev.awery.ui.mobile.screens.IntentHandlerActivity
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity
import com.mrboomdev.awery.ui.tv.TvExperimentsActivity
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.util.ui.markdown.LinkifyPlugin
import com.mrboomdev.awery.util.ui.markdown.SpoilerPlugin
import com.mrboomdev.awery.utils.buildIntent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.mihon.injekt.patchInjekt
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.WeakHashMap

private const val TAG = "App"
private val backPressedCallbacks = WeakHashMap<Runnable, Any>()

class App : Application() {

	override fun attachBaseContext(base: Context) {
		AndroidGlobals.applicationContext = this
		super.attachBaseContext(base)
		CrashHandler.setup()
	}

	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreate() {
		applyTheme()
		super.onCreate()
		setupStrictMode()
		
		GlobalScope.launch(Dispatchers.Default) {
			initSync()
		}
	}
	
	/**
	 * Long-time performing operations are performing here.
	 * Don't forget to call this method to init everything!
	 */
	private fun initSync() {
		didInit = false
		
		patchInjekt()
		registerNotificationChannels()
		BigImageViewer.initialize(GlideCustomImageLoader.with(this))
		
		// Material you isn't available on tv, so we do reset an value to something else.
		if(AwerySettings.THEME_COLOR_PALETTE.value == AwerySettings.ThemeColorPaletteValue.MATERIAL_YOU && isTv) {
			AwerySettings.THEME_COLOR_PALETTE.value = AwerySettings.ThemeColorPaletteValue.RED
		}
		
		// TODO: Replace this stupid fix when proper settings would be implemented everywhere.
		if(AwerySettings.USE_DARK_THEME.value == null) {
			AwerySettings.USE_DARK_THEME.value = ThemeManager.isDarkModeEnabled
		}

		if(AwerySettings.LAST_OPENED_VERSION.value < 1) {
			CoroutineScope(Dispatchers.IO).launch {
				database.listDao.insert(
					DBCatalogList.fromCatalogList(CatalogList(i18n(Res.string.currently_watching), "1")),
					DBCatalogList.fromCatalogList(CatalogList(i18n(Res.string.planning_watch), "2")),
					DBCatalogList.fromCatalogList(CatalogList(i18n(Res.string.delayed), "3")),
					DBCatalogList.fromCatalogList(CatalogList(i18n(Res.string.completed), "4")),
					DBCatalogList.fromCatalogList(CatalogList(i18n(Res.string.dropped), "5")),
					DBCatalogList.fromCatalogList(CatalogList(i18n(Res.string.favourites), "6")),
					DBCatalogList.fromCatalogList(CatalogList("Hidden", Constants.CATALOG_LIST_BLACKLIST)),
					DBCatalogList.fromCatalogList(CatalogList("History", Constants.CATALOG_LIST_HISTORY))
				)
				
				AwerySettings.LAST_OPENED_VERSION.value = 1
			}
		}
		
		if(isTv) {
			// Tv doesn't show up any shortcuts, so we have to show an separate app launcher.
			packageManager.setComponentEnabledSetting(
				ComponentName(this, TvExperimentsActivity::class.java),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
		} else {
			ShortcutManagerCompat.pushDynamicShortcut(applicationContext,
				ShortcutInfoCompat.Builder(this, "experiments")
					.setIcon(IconCompat.createWithResource(this, R.drawable.ic_experiment_outlined))
					.setLongLabel("Open experimental settings")
					.setShortLabel("Experiments")
					.setLongLived(true)
					.setIntent(Intent(this, IntentHandlerActivity::class.java).apply {
						action = Intent.ACTION_VIEW
						data = Uri.parse("awery://experiments")
					}).build())
		}
		
		didInit = true
	}

	companion object {
		var didInit = false 
			private set

		private val globalMoshi: Moshi by lazy {
			Moshi.Builder()
				.addLast(KotlinJsonAdapterFactory())
				.build()
		}

		val database: AweryDB by lazy {
			databaseBuilder(AndroidGlobals.applicationContext, AweryDB::class.java, "db")
				.addMigrations(AweryDB.MIGRATION_2_3, AweryDB.MIGRATION_3_4)
				.build()
		}
		
		@Deprecated("Use kotlin serialization instead!")
		fun getMoshi(): Moshi {
			return globalMoshi
		}

		@Deprecated("Use kotlin serialization instead!")
		fun getMoshi(vararg adapters: Any): Moshi {
			if(adapters.isEmpty()) {
				return globalMoshi
			}

			return Moshi.Builder()
				.apply {
					for(adapter in adapters) {
						add(adapter)
					}
				}
				.addLast(KotlinJsonAdapterFactory())
				.build()
		}

		@JvmStatic
		fun getMarkwon(context: Context): Markwon {
			return Markwon.builder(context)
				.usePlugin(SoftBreakAddsNewLinePlugin())
				.usePlugin(StrikethroughPlugin.create())
				.usePlugin(GlideImagesPlugin.create(context))
				.usePlugin(LinkifyPlugin.create())
				.usePlugin(SpoilerPlugin.create())
				.usePlugin(HtmlPlugin.create())
				.build()
		}

		fun copyToClipboard(content: Uri) {
			copyToClipboard(ClipData.newRawUri(null, content))
		}

		fun copyToClipboard(content: String) {
			copyToClipboard(ClipData.newPlainText(null, content))
		}

		fun copyToClipboard(clipData: ClipData) {
			AndroidGlobals.applicationContext.getSystemService<ClipboardManager>()!!.setPrimaryClip(clipData)

			// Android 13 and higher shows a visual confirmation of copied contents
			// https://developer.android.com/about/versions/13/features/copy-paste
			if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
				toast(i18n(Res.string.copied_to_clipboard))
			}
		}

		@JvmStatic
		fun share(text: String) {
			IntentBuilder(anyContext)
				.setType("text/plain")
				.setText(text)
				.startChooser()
		}

		@JvmStatic
		@Deprecated("Old java shit")
		fun getResourceId(type: Class<*>, res: String?): Int {
			if(res == null) return 0

			try {
				val field = type.getDeclaredField(res)
				field.isAccessible = true
				val result = field[null]

				if(result == null) {
					Log.e(TAG, "Resource id \"" + res + "\" was not initialized in \"" + type.name + "\"!")
					return 0
				}

				return result as Int
			} catch(e: NoSuchFieldException) {
				return 0
			} catch(e: IllegalAccessException) {
				throw IllegalStateException(
					"Generated resource id filed cannot be private! Check if the provided class is the R class", e
				)
			}
		}

		/**
		 * There is a bug in an appcompat library which sometimes throws an [NullPointerException].
		 * This method tries to do it without throwing any exceptions.
		 */
		@JvmStatic
		@Deprecated("Old java shit")
		fun setContentViewCompat(activity: Activity, view: View) {
			try {
				activity.setContentView(view)
			} catch(e: NullPointerException) {
				Log.e(TAG, "Failed to setContentView!", e)

				// Caused by: java.lang.NullPointerException: Attempt to invoke virtual method
				//     'void androidx.appcompat.widget.ContentFrameLayout.setDecorPadding(int, int, int, int)' on a null object reference

				// at androidx.appcompat.app.AppCompatDelegateImpl.applyFixedSizeWindow(AppCompatDelegateImpl)
				AweryLifecycle.postRunnable { setContentViewCompat(activity, view) }
			}
		}

		/**
		 * Safely enables the "Edge to edge" experience.
		 * I really don't know why, but sometimes it just randomly crashes!
		 * Because of it we have to rerun this method on a next frame.
		 * @author MrBoomDev
		 */
		@JvmStatic
		@Deprecated("Old java shit")
		fun enableEdgeToEdge(context: ComponentActivity) {
			try {
				context.enableEdgeToEdge()
			} catch(e: RuntimeException) {
				Log.e(TAG, "Failed to enable EdgeToEdge! Will retry a little bit later.", e)
				AweryLifecycle.postRunnable { enableEdgeToEdge(context) }
			}
		}

		@JvmStatic
		@Deprecated("Old java shit")
		fun removeOnBackPressedListener(activity: Activity, callback: Runnable) {
			val onBackInvokedCallback = backPressedCallbacks.remove(callback) ?: return

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(
					(onBackInvokedCallback as OnBackInvokedCallback)
				)
			} else {
				if(onBackInvokedCallback is OnBackPressedCallback) onBackInvokedCallback.remove()
				else throw IllegalArgumentException("Callback must implement OnBackPressedCallback!")
			}
		}

		@JvmStatic
		@Deprecated("Old java shit")
		fun addOnBackPressedListener(activity: Activity, callback: Runnable) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				val onBackInvokedCallback = OnBackInvokedCallback { callback.run() }
				backPressedCallbacks[callback] = onBackInvokedCallback

				val dispatcher = activity.onBackInvokedDispatcher
				dispatcher.registerOnBackInvokedCallback(0, onBackInvokedCallback)
			} else {
				if(activity is OnBackPressedDispatcherOwner) {
					val onBackInvokedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							callback.run()
						}
					}

					activity.onBackPressedDispatcher.addCallback(activity, onBackInvokedCallback)
					backPressedCallbacks[callback] = onBackInvokedCallback
				} else {
					throw IllegalArgumentException("Activity must implement OnBackPressedDispatcherOwner!")
				}
			}
		}

		@JvmStatic
		@Deprecated("Old java shit")
		fun resolveAttrColor(context: Context, @AttrRes res: Int): Int {
			return MaterialColors.getColor(context, res, Color.BLACK)
		}

		@JvmStatic
		@Deprecated("Old java shit")
		@SuppressLint("RestrictedApi")
		fun resolveAttr(context: Context?, @AttrRes res: Int): TypedValue? {
			return MaterialAttributes.resolve(context!!, res)
		}

		@JvmStatic
		fun isLandscape(context: Context): Boolean {
			return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
		}

		@JvmStatic
		val isLandscape: Boolean
			get() = isLandscape(AndroidGlobals.applicationContext)

		@JvmStatic
		@JvmOverloads
		fun openUrl(context: Context, url: String, forceInternal: Boolean = false) = with(context) {
			if(forceInternal) {
				startActivity(buildIntent(BrowserActivity::class, BrowserActivity.Extras(url)))
				return@with
			}

			val customTabsIntent = CustomTabsIntent.Builder().apply {
				setColorScheme(
					if(ThemeManager.isDarkModeEnabled) CustomTabsIntent.COLOR_SCHEME_DARK
					else CustomTabsIntent.COLOR_SCHEME_LIGHT)
			}.build().apply {
				intent.data = Uri.parse(url)
			}

			customTabsIntent.intent.resolveActivity(packageManager)?.also {
				startActivity(customTabsIntent.intent, customTabsIntent.startAnimationBundle)
			} ?: run {
				Log.e(TAG, "No external browser was found, launching a internal one.")
				startActivity(buildIntent(BrowserActivity::class, BrowserActivity.Extras(url)))
			}
		}

		/**
		 * Fuck you, Android. It's not my problem that some people do install A LOT of extensions,
		 * so that app stops responding.
		 */
		private fun setupStrictMode() {
			if(!BuildConfig.DEBUG) {
				StrictMode.setThreadPolicy(ThreadPolicy.LAX)
				StrictMode.setVmPolicy(VmPolicy.LAX)
				return
			}

			StrictMode.setThreadPolicy(ThreadPolicy.Builder()
				.detectCustomSlowCalls()
				.detectNetwork()
				.penaltyLog()
				.penaltyDialog()
				.build())

			StrictMode.setVmPolicy(VmPolicy.Builder()
				.setClassInstanceLimit(SettingsActivity::class.java, 10)
				.detectActivityLeaks()
				.detectLeakedRegistrationObjects()
				.penaltyLog()
				.apply {
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
						penaltyListener({ it.run() }) { violation ->
							try {
								runOnUiThread { DialogBuilder(getAnyActivity<AppCompatActivity>()!!)
									.setTitle("StrictMode.VmPolicy Violation!")
									.setMessage(Log.getStackTraceString(violation))
									.setPositiveButton(i18n(Res.string.ok)) { it.dismiss() }
									.show() }
							} catch(e: Throwable) {
								Log.e(TAG, "Failed to warn about an strict mode violation!", e)
							}
						}
					}
				}.build())
		}

		@JvmStatic
		val orientation: Int
			get() = Resources.getSystem().configuration.orientation

		@JvmStatic
		val navigationStyle: AwerySettings.NavigationStyleValue
			get() = AwerySettings.NAVIGATION_STYLE.value.let {
				if(isTv) AwerySettings.NavigationStyleValue.MATERIAL else it
			}

		@JvmStatic
		@JvmOverloads
		fun snackbar(activity: Activity, title: Any?, button: Any?, buttonCallback: Runnable?, duration: Int = Snackbar.LENGTH_LONG) {
			runOnUiThread {
				val titleText = title?.toString() ?: "null"
				val buttonText = button?.toString() ?: "null"

				val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
				val snackbar = Snackbar.make(rootView, titleText, duration)

				if(buttonCallback != null) {
					snackbar.setAction(buttonText) { buttonCallback.run() }
				}

				snackbar.view.setOnClickListener { snackbar.dismiss() }
				snackbar.show()
			}
		}

		@JvmStatic
		fun showLoadingWindow(): Dialog {
			val context = anyContext

			val wrapper = LinearLayoutCompat(context)
			wrapper.gravity = Gravity.CENTER

			val progress = CircularProgressIndicator(context)
			progress.isIndeterminate = true
			wrapper.addView(progress)

			val dialog = AlertDialog.Builder(context)
				.setCancelable(false)
				.setView(wrapper)
				.show()

			dialog.window!!.setBackgroundDrawable(null)
			return dialog
		}

		fun isRequirementMet(requirement: String): Boolean {
			var mRequirement = requirement
			var invert = false

			if(mRequirement.startsWith("!")) {
				invert = true
				mRequirement = mRequirement.substring(1)
			}

			val result = when(mRequirement) {
				"material_you" -> DynamicColors.isDynamicColorAvailable()
				"tv" -> isTv
				"beta" -> BuildConfig.CHANNEL != UpdatesChannel.STABLE
				"debug" -> BuildConfig.DEBUG
				"never" -> false
				else -> true
			}

			if(invert) {
				return !result
			}

			return result
		}
	}
}