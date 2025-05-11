package com.mrboomdev.awery.ext.util.exceptions

import com.mrboomdev.awery.ext.util.LocaleAware

class ZeroResultsException(
    message: String? = null,
    cause: Throwable?,
    private val detailedMessage: String? = null
) : Exception(message, cause), LocaleAware {
    constructor(message: String? = null, detailedMessage: String? = null): this(message, null, detailedMessage)
    override fun getLocalizedMessage() = detailedMessage ?: message
}