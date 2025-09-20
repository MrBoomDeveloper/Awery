package com.mrboomdev.awery.extension.sdk

open class ExtensionLoadException(
    message: String? = null,
    cause: Throwable? = null
): Exception(message, cause) {
    class IllegalArchitecture(
        message: String? = null,
        cause: Throwable? = null
    ): ExtensionLoadException(message, cause)

    class UnsupportedLibVersion(
        val version: String,
        val minVersion: String,
        val maxVersion: String,
        message: String? = null,
        cause: Throwable? = null
    ): ExtensionLoadException(message, cause)

    class Disabled(
        message: String? = null,
        cause: Throwable? = null
    ): ExtensionLoadException(message, cause)
}