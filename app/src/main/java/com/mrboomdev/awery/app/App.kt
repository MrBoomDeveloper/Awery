package com.mrboomdev.awery.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import android.widget.Toast
import android.window.OnBackInvokedCallback
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.core.content.getSystemService
import androidx.room.Room.databaseBuilder
import androidx.viewbinding.ViewBinding
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideCustomImageLoader
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.resources.MaterialAttributes
import com.google.android.material.snackbar.Snackbar
import com.mrboomdev.awery.BuildConfig
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.app.data.Constants
import com.mrboomdev.awery.app.data.db.AweryDB
import com.mrboomdev.awery.app.data.db.item.DBCatalogList
import com.mrboomdev.awery.app.data.settings.NicePreferences.getPrefs
import com.mrboomdev.awery.extensions.data.CatalogList
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.generated.AwerySettings.NavigationStyle_Values
import com.mrboomdev.awery.ui.activity.BrowserActivity
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity
import com.mrboomdev.awery.util.extensions.configuration
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.util.ui.markdown.LinkifyPlugin
import com.mrboomdev.awery.util.ui.markdown.SpoilerPlugin
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.mihon.injekt.patchInjekt
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.WeakHashMap
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class App : Application() {

	override fun attachBaseContext(base: Context) {
		appContext = this
		super.attachBaseContext(base)
		CrashHandler.setupCrashListener(this)
	}

	override fun onCreate() {
		AweryNotifications.registerNotificationChannels()
		ThemeManager.applyApp(this)
		setupStrictMode()

		super.onCreate()
		patchInjekt()
		BigImageViewer.initialize(GlideCustomImageLoader.with(this))

		// Note: I'm so sorry. I've just waste the whole day to try fixing THIS SHIT!!!!
		// And in result in nothing! FUCKIN LIGHT THEME! WHY DOES IT EXIST!?!?!?!?!?!?!?!?!?!?
		// SYKA BLYYYYYYAAAAAAAT
		AwerySettings.USE_DARK_THEME.getValue(ThemeManager.isDarkModeEnabled())

		if(AwerySettings.LOG_NETWORK.value) {
			val logFile = File(getExternalFilesDir(null), "okhttp3_log.txt")
			logFile.delete()

			try {
				logFile.createNewFile()

				Logger.getLogger(OkHttpClient::class.java.name).addHandler(object : Handler() {
					override fun publish(record: LogRecord) {
						try {
							FileWriter(logFile, true).use { writer ->
								writer.write("[" + record.level + "] " + record.message + "\n")
							}
						} catch(e: IOException) {
							Log.e(TAG, "Failed to write log file!", e)
						}
					}

					override fun flush() {}
					override fun close() {}
				})
			} catch(e: IOException) {
				Log.e(TAG, "Failed to create log file!", e)
			}
		}

		if(AwerySettings.LAST_OPENED_VERSION.value < 1) {
			CoroutineScope(Dispatchers.IO).launch {
				database.listDao.insert(
					DBCatalogList.fromCatalogList(CatalogList(getString(R.string.currently_watching), "1")),
					DBCatalogList.fromCatalogList(CatalogList(getString(R.string.planning_watch), "2")),
					DBCatalogList.fromCatalogList(CatalogList(getString(R.string.delayed), "3")),
					DBCatalogList.fromCatalogList(CatalogList(getString(R.string.completed), "4")),
					DBCatalogList.fromCatalogList(CatalogList(getString(R.string.dropped), "5")),
					DBCatalogList.fromCatalogList(CatalogList(getString(R.string.favourites), "6")),
					DBCatalogList.fromCatalogList(CatalogList("Hidden", Constants.CATALOG_LIST_BLACKLIST)),
					DBCatalogList.fromCatalogList(CatalogList("History", Constants.CATALOG_LIST_HISTORY))
				)

				getPrefs().setValue(AwerySettings.LAST_OPENED_VERSION, 1).saveSync()
			}
		}
	}

	companion object {
		private val backPressedCallbacks = WeakHashMap<Runnable, Any>()
		private const val TAG = "App"

		private val globalMoshi: Moshi by lazy {
			Moshi.Builder()
				.addLast(KotlinJsonAdapterFactory())
				.build()
		}

		val database: AweryDB by lazy {
			databaseBuilder(appContext, AweryDB::class.java, "db")
				.addMigrations(AweryDB.MIGRATION_2_3, AweryDB.MIGRATION_3_4)
				.build()
		}

		fun getMoshi(): Moshi {
			return globalMoshi
		}

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
			appContext.getSystemService<ClipboardManager>()!!.setPrimaryClip(clipData)

			// Android 13 and higher shows a visual confirmation of copied contents
			// https://developer.android.com/about/versions/13/features/copy-paste
			if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
				toast(R.string.copied_to_clipboard)
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
		@Deprecated("")
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

		@JvmStatic
		fun i18n(clazz: Class<*>, string: String?): String? {
			val id = getResourceId(clazz, string)
			return if(id == 0) null else i18n(id)
		}

		@JvmStatic
		fun i18n(@StringRes res: Int): String {
			return appContext.getString(res)
		}

		@JvmStatic
		@JvmOverloads
		fun toast(context: Context?, text: Any?, duration: Int = 0) {
			runOnUiThread { Toast.makeText(context, text?.toString() ?: "null", duration).show() }
		}

		@JvmStatic
		@JvmOverloads
		fun toast(text: Any?, duration: Int = 0) {
			toast(appContext, text, duration)
		}

		@JvmStatic
		fun toast(@StringRes res: Int) {
			toast(appContext.getString(res))
		}

		@JvmStatic
		fun toast(@StringRes res: Int, duration: Int) {
			toast(i18n(res), duration)
		}

		/**
		 * There is a bug in an appcompat library which sometimes throws an [NullPointerException].
		 * This method tries to do it without throwing any exceptions.
		 */
		@JvmStatic
		@Deprecated("")
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

		@JvmStatic
		@Deprecated("")
		fun setContentViewCompat(activity: Activity, view: ViewBinding) {
			setContentViewCompat(activity, view.root)
		}

		/**
		 * Safely enables the "Edge to edge" experience.
		 * I really don't know why, but sometimes it just randomly crashes!
		 * Because of it we have to rerun this method on a next frame.
		 * @author MrBoomDev
		 */
		@JvmStatic
		@Deprecated("")
		fun enableEdgeToEdge(context: ComponentActivity) {
			try {
				context.enableEdgeToEdge()
			} catch(e: RuntimeException) {
				Log.e(TAG, "Failed to enable EdgeToEdge! Will retry a little bit later.", e)
				AweryLifecycle.postRunnable { enableEdgeToEdge(context) }
			}
		}

		@JvmStatic
		@Deprecated("")
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
		@Deprecated("")
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
		@Deprecated("")
		fun resolveAttrColor(context: Context, @AttrRes res: Int): Int {
			return MaterialColors.getColor(context, res, Color.BLACK)
		}

		@JvmStatic
		@Deprecated("")
		@SuppressLint("RestrictedApi")
		fun resolveAttr(context: Context?, @AttrRes res: Int): TypedValue? {
			return MaterialAttributes.resolve(context!!, res)
		}

		@JvmStatic
		fun isLandscape(context: Context): Boolean {
			return context.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
		}

		@JvmStatic
		val isLandscape: Boolean
			get() = isLandscape(appContext)

		@JvmStatic
		val isTv by lazy {
			appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
		}

		@JvmStatic
		@JvmOverloads
		fun openUrl(context: Context, url: String, forceInternal: Boolean = false) {
			if(forceInternal) {
				context.startActivity(BrowserActivity::class, BrowserActivity.Extras(url))
				return
			}

			val customTabsIntent = CustomTabsIntent.Builder()
				.setColorScheme(
					if(ThemeManager.isDarkModeEnabled()) CustomTabsIntent.COLOR_SCHEME_DARK
					else CustomTabsIntent.COLOR_SCHEME_LIGHT).build()

			customTabsIntent.intent.setData(Uri.parse(url))

			val resolvedActivity = customTabsIntent.intent
				.resolveActivity(context.packageManager)

			if(resolvedActivity != null) {
				context.startActivity(customTabsIntent.intent, customTabsIntent.startAnimationBundle)
			} else {
				Log.e(TAG, "No external browser was found, launching a internal one.")
				context.startActivity(BrowserActivity::class, BrowserActivity.Extras(url))
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
							runOnUiThread { DialogBuilder(getAnyActivity<AppCompatActivity>())
								.setTitle("StrictMode.VmPolicy Violation!")
								.setMessage(Log.getStackTraceString(violation))
								.setPositiveButton(R.string.ok) { it.dismiss() }
								.show() }
						}
					}
				}.build())
		}

		@JvmStatic
		val orientation: Int
			get() = Resources.getSystem().configuration.orientation

		@JvmStatic
		val navigationStyle: NavigationStyle_Values
			get() = AwerySettings.NAVIGATION_STYLE.value.let {
				if((it == null || isTv)) NavigationStyle_Values.MATERIAL else it
			}

		@Deprecated(message = "java shit")
		fun getConfiguration(context: Context): Configuration {
			return context.configuration
		}

		val configuration: Configuration
			get() = anyContext.configuration

		@JvmStatic
		@JvmOverloads
		fun snackbar(
			activity: Activity,
			@StringRes title: Int,
			@StringRes button: Int,
			buttonCallback: Runnable?,
			duration: Int = Snackbar.LENGTH_SHORT
		) {
			snackbar(activity, activity.getString(title), activity.getString(button), buttonCallback, duration)
		}

		@JvmStatic
		fun snackbar(activity: Activity, title: Any?, button: Any?, buttonCallback: Runnable?, duration: Int) {
			AweryLifecycle.runOnUiThread {
				val titleText = title?.toString() ?: "null"
				val buttonText = button?.toString() ?: "null"

				val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
				val snackbar = Snackbar.make(rootView, titleText, duration)

				if(buttonCallback != null) {
					snackbar.setAction(buttonText) { view: View? -> buttonCallback.run() }
				}

				snackbar.view.setOnClickListener { v: View? -> snackbar.dismiss() }
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
	}
}