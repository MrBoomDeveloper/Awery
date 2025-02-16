package com.mrboomdev.awery.platform

import android.annotation.SuppressLint
import android.app.Application
import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.google.android.material.color.DynamicColors
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.shared.BuildConfig
import com.mrboomdev.awery.utils.SerializableRequired
import com.mrboomdev.awery.utils.tryOrTry
import com.mrboomdev.safeargsnext.value.serializableFunction
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

private const val TAG = "Platform.android"

actual object Platform: ContextWrapper(null) {
	actual val NAME = "Android ${Build.VERSION.RELEASE}"
	
	val MIUI by lazy {
		getSystemProperty("ro.miui.ui.version.name")?.isNotEmpty() ?: false // MIUI
				|| getSystemProperty("ro.mi.os.version.name")?.isNotEmpty() ?: false // HyperOS
	}
	
	val SAMSUNG by lazy {
		Build.MANUFACTURER.equals("samsung", ignoreCase = true)
	}
	
	actual val CACHE_DIRECTORY: File
		get() = cacheDir

	@Suppress("DEPRECATION")
	actual val TV by lazy { 
		packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) 
				|| packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
				|| getSystemService<UiModeManager>()!!.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
				|| !packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
	}
	
	private val handler by lazy {
		Handler(Looper.getMainLooper())
	}

	actual fun isRequirementMet(requirement: String): Boolean {
		var mRequirement = requirement
		var invert = false

		if(mRequirement.startsWith("!")) {
			invert = true
			mRequirement = mRequirement.substring(1)
		}

		val result = when(mRequirement) {
			"material_you" -> DynamicColors.isDynamicColorAvailable()
			"tv" -> TV
			"beta" -> packageName != "com.mrboomdev.awery"
			"debug" -> BuildConfig.DEBUG
			else -> false
		}

		return if(invert) !result else result
	}
	
	actual fun exitApp() {
		@OptIn(SerializableRequired::class)
		executeInActivity(serializableFunction { activity ->
			activity.finishAffinity()
		})
	}
	
	actual fun restartApp() {
		Log.i(TAG, "restartApp() has been invoked!")
		
		startActivity(Intent.makeRestartActivityTask(
			packageManager.getLaunchIntentForPackage(packageName)!!.component
		).apply { setPackage(packageName) })
		
		exitProcess(0)
	}
	
	fun toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
		handler.postDelayed({
			Toast.makeText(applicationContext, text.toString(), duration).show()
		}, 1L)
	}
	
	actual fun getSharedPreferences(name: String): SharedPreferences = 
		getSharedPreferences(name, 0)
	
	override fun getApplicationContext(): Application {
		return baseContext as Application
	}
	
	fun attachBaseContext(base: Application) {
		super.attachBaseContext(base)
	}
	
	/**
	 * @return true if an system popup just appeared or false otherwise.
	 */
	actual fun copyToClipboard(string: String): Boolean {
		getSystemService<ClipboardManager>()!!.setPrimaryClip(
			ClipData.newPlainText(null, string))
		
		// Android 13 and higher shows a visual confirmation of copied contents
		// https://developer.android.com/about/versions/13/features/copy-paste
		return Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2
	}
	
	@SuppressLint("PrivateApi")
	private fun getSystemProperty(property: String) = tryOrTry(
		{
			Class.forName("android.os.SystemProperties")
				.getDeclaredMethod("get", String::class.java)
				.invoke(null, property) as String
		}, {
			Runtime.getRuntime().exec("getprop $property").inputStream.use {
				BufferedReader(InputStreamReader(it), 1024).readLine()
			}
		}
	)
	
	actual val USER_AGENT: String
		get() = buildString {
			append("Mozilla/5.0 (Linux; Android ")
			append(Build.VERSION.RELEASE)
			append("; Pixel 6) AppleWebKit/537.36(KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36")
		}
	
	/**
	 * Initialize platform-related stuff
	 */
	internal actual suspend fun platformInit() {
		setupStrictMode()
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
		
		StrictMode.setThreadPolicy(
			ThreadPolicy.Builder()
			.detectCustomSlowCalls()
			.detectNetwork()
			.penaltyLog()
			.penaltyDialog()
			.build())
		
		StrictMode.setVmPolicy(
			VmPolicy.Builder()
			.detectActivityLeaks()
			.detectLeakedRegistrationObjects()
			.penaltyLog()
			.apply {
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					penaltyListener({ it.run() }) { violation ->
						toast("An VM Policy has benn violated!")
						Log.e(TAG, "An VM Policy has benn violated!", violation)
					}
				}
			}.build())
	}
}