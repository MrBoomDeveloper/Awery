package com.mrboomdev.awery.platform

import android.util.Log
import android.widget.Toast
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.android.AndroidGlobals
import com.mrboomdev.awery.platform.android.AndroidGlobals.isTv
import com.mrboomdev.awery.platform.android.AndroidGlobals.toast
import com.mrboomdev.awery.shared.BuildConfig
import xcrash.Errno
import xcrash.XCrash
import java.io.File

private const val TAG = "CrashHandler"

actual object CrashHandler {
	
	private enum class CrashType {
		ANR, JAVA, NATIVE
	}
	
	actual fun setup() {
		when(XCrash.init(AndroidGlobals.applicationContext, XCrash.InitParameters().apply {
			// Sometimes exoplayer does throw some native exceptions in the background
			// and XCrash catches it for no reason, so we don't catch any native exceptions.
			disableNativeCrashHandler()
			
			// This library doesn't check if an ANR has happened
			// properly on Android TV, so we disable it.
			setAnrCheckProcessState(!isTv)
			
			// Crash logs are too long so we do strip all non-relevant dumps.
			setJavaDumpNetworkInfo(false)
			setJavaDumpFds(false)
			setJavaDumpAllThreads(false)
			
			setAnrDumpFds(false)
			setAnrDumpNetwork(false)
			
			setNativeDumpFds(false)
			setNativeDumpMap(false)
			setNativeDumpNetwork(false)
			setNativeDumpElfHash(false)
			setNativeDumpAllThreads(false)
			
			// Logcat? There is only some shit...
			setJavaLogcatMainLines(0)
			setJavaLogcatEventsLines(0)
			setJavaLogcatSystemLines(0)
			
			setAnrLogcatMainLines(0)
			setAnrLogcatEventsLines(0)
			setAnrLogcatSystemLines(0)
			
			setNativeLogcatMainLines(0)
			setNativeLogcatEventsLines(0)
			setNativeLogcatSystemLines(0)
			
			// Setup crash handlers
			setJavaCallback { _, message -> handleError(CrashType.JAVA, message) }
			setNativeCallback { _, message -> handleError(CrashType.NATIVE, message) }
			setAnrCallback { _, message -> handleError(CrashType.ANR, message) }
			
			// While debugging an ANR may be triggered, so we disable it in the dev build.
			if(BuildConfig.DEBUG) {
				setAnrRethrow(false)
				setAnrCallback(null)
				setAnrFastCallback(null)
			}
		})) {
			Errno.INIT_LIBRARY_FAILED -> "Failed to initialize XCrash library!"
			Errno.LOAD_LIBRARY_FAILED -> "Failed to load XCrash library!"
			else -> ""
		}.let {
			if(it.isBlank()) return
			toast(it, Toast.LENGTH_LONG)
			Log.e(TAG, it)
		}
	}
	
	private fun handleError(type: CrashType, message: String?) {
		toast(i18n(when(type) {
			CrashType.ANR -> {
				Log.e(TAG, "ANR error has happened. $message")
				Res.string.app_not_responding_restart
			}
			
			CrashType.JAVA -> Res.string.app_crash
			CrashType.NATIVE -> Res.string.something_terrible_happened
		}), 1)
		
		AndroidGlobals.restartApp()
	}
	
	private val crashLogsDirectory: File
		get() = File(AndroidGlobals.applicationContext.filesDir, "tombstones")
	
	actual val crashLogs: List<File>
		get() = crashLogsDirectory.listFiles()?.sortedBy { it.lastModified() }?.reversed() ?: emptyList()
}