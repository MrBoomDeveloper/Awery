package com.mrboomdev.awery.ext.util.exceptions

class ExtensionLoadException(
	message: String? = null,
	cause: Throwable? = null,
	val reason: Int
) : Exception(message, cause) {
	companion object {
		const val REASON_OTHER = 0
		const val REASON_NSFW_BLOCKED = 1
		const val REASON_INVALID = 2
	}
}