package com.mrboomdev.awery.platform

import java.io.File

expect object Platform {
	val NAME: String
	val CACHE_DIRECTORY: File
	fun exitApp()
	fun restartApp()
	fun isRequirementMet(requirement: String): Boolean
}

fun areRequirementsMet(requirements: Array<String>): Boolean {
	for(requirement in requirements) {
		if(!Platform.isRequirementMet(requirement)) return false
	}

	return true
}