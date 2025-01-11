package com.mrboomdev.awery.ext.util.exceptions

import com.mrboomdev.awery.ext.util.LocaleAware

class ExtensionInstallException(
	message: String? = null,
	cause: Throwable? = null,
	val userReadableMessage: String? = null
) : Exception(message, cause), LocaleAware {
	override fun getLocalizedMessage() = userReadableMessage ?: message
}