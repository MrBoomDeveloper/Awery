package com.mrboomdev.awery.platform.android

import android.app.Application
import android.app.UiModeManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import com.mrboomdev.awery.utils.SerializableRequired
import com.mrboomdev.safeargsnext.value.serializableFunction
import kotlin.system.exitProcess

private const val TAG = "AndroidGlobals"

object AndroidGlobals {
	lateinit var applicationContext: Application
	
	private val handler by lazy {
		Handler(Looper.getMainLooper())
	}
	
	@Suppress("DEPRECATION")
	val isTv by lazy {
		with(applicationContext) {
			packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) 
					|| packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
					|| getSystemService<UiModeManager>()!!.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
					|| !packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
		}
	}
	
	fun toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
		handler.postDelayed({
			Toast.makeText(applicationContext, text.toString(), duration).show()
		}, 1L)
	}
	
	fun restartApp() {
		Log.i(TAG, "restartApp() has been invoked!")
		
		applicationContext.startActivity(
			Intent.makeRestartActivityTask(
				applicationContext.packageManager.getLaunchIntentForPackage(
					applicationContext.packageName
				)!!.component
			).apply { 
				setPackage(applicationContext.packageName) 
			}
		)
		
		exitProcess(0)
	}
	
	fun exitApp() {
		@OptIn(SerializableRequired::class)
		applicationContext.executeInActivity(serializableFunction { activity ->
			activity.finishAffinity()
		})
	}
}