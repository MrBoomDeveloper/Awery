package com.mrboomdev.awery.ext.util.exceptions

class ExtensionInstallException(
	message: String? = null,
	cause: Throwable? = null,
	val userReadableMessage: String? = null
) : Exception(message, cause)