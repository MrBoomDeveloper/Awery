package com.mrboomdev.awery.platform

actual object Platform {
	actual val NAME = "Desktop"

	actual fun isRequirementMet(requirement: String): Boolean {
		return false
	}
}