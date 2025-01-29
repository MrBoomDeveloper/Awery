package com.mrboomdev.awery.platform

import java.io.File
import kotlin.system.exitProcess

actual object Platform {
	actual val NAME = "Desktop"
	
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
}