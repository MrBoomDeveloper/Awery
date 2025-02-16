package com.mrboomdev.awery.platform

import com.mrboomdev.awery.SharedPreferences
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import kotlin.system.exitProcess

actual object Platform {
	actual val NAME = "Desktop"
	actual val TV = false
	
	actual val CACHE_DIRECTORY: File
		get() = TODO("Not yet implemented")

	actual fun isRequirementMet(requirement: String): Boolean {
		return false
	}
	
	actual fun exitApp() {
		exitProcess(0)
	}
	
	actual fun restartApp() {
		TODO("IMPLEMENT AN APP RESTART PROCESS ON DESKTOP")
	}
	
	actual fun getSharedPreferences(name: String): SharedPreferences {
		TODO("Not yet implemented")
	}
	
	/**
	 * @return true if an system popup just appeared or false otherwise.
	 */
	actual fun copyToClipboard(string: String): Boolean {
		Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(string), null)
		return false
	}
	
	actual val USER_AGENT: String
		get() = "Mozilla/5.0 (Linux; Android 14; Pixel 6) AppleWebKit/537.36(KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36"
	
	/**
	 * Initialize platform-related stuff
	 */
	internal actual suspend fun platformInit() {}
}