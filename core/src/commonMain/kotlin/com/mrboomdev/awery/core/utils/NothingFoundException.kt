package com.mrboomdev.awery.core.utils

class NothingFoundException(
    message: String? = null,
    cause: Throwable? = null
): Exception(message, cause)