package com.mrboomdev.awery.platform

import kotlin.system.exitProcess

actual object Platform {
	actual val NAME = "Desktop"

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