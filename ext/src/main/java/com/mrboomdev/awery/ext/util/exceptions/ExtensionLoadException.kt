package com.mrboomdev.awery.ext.util.exceptions

class ExtensionLoadException(
	message: String? = null,
	cause: Throwable? = null,
	val userReadableMessage: String? = null
) : Exception(message, cause)