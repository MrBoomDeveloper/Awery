package com.mrboomdev.awery.ext.util.exceptions

class ExtensionInstallException(
	message: String? = null,
	cause: Throwable? = null,
	val reason: Int
) : Exception(message, cause) {
	companion object {
		const val REASON_OTHER = 0
		const val REASON_UNSUPPORTED = 1
		const val REASON_LOW_STORAGE = 2
		const val REASON_NSFW_BLOCKED = 3
		const val REASON_INVALID = 4
	}
}