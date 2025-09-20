package com.mrboomdev.awery.core.utils

private const val DEFAULT_TAG = "Awery"
private const val DISALLOWED_CLASS_NAME_PREFIX = "com.mrboomdev.awery.core.utils.Log"

/**
 * A multiplatform logging utility.
 *
 * This object provides a common interface for logging messages across different platforms.
 * It offers standard logging levels: info, debug, warning, and error.
 *
 * Example usage:
 * ```kotlin
 * Log.i("MyTag", "This is an informational message.")
 * Log.e("MyTag", "An error occurred", exception)
 * ```
 */
expect object Log {
    /**
     * Sends an info log message.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log call occurs.
     * @param message The message you would like logged.
     */
    fun i(tag: String, message: String)

    /**
     * Sends a debug message to the log.
     *
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log call occurs.
     * @param message The message to log.
     */
    fun d(tag: String, message: String)

    /**
     * Sends a warning log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log call occurs.
     * @param message The message you would like logged.
     */
    fun w(tag: String, message: String)

    /**
     * Sends an error log message.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log call occurs.
     * @param message The message you would like logged.
     * @param throwable An exception to log.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

class Logger internal constructor(val tag: String) {
    fun i(message: String) = Log.i(tag, message)
    fun d(message: String) = Log.d(tag, message)
    fun w(message: String) = Log.w(tag, message)
    fun e(message: String, throwable: Throwable? = null) = Log.e(tag, message, throwable)
}

class LoggerDelegate internal constructor() {
    private var cachedLogger: Logger? = null

    operator fun getValue(thisRef: Any?, property: Any?): Logger {
        cachedLogger?.also { return it }
        val tag = thisRef?.let { this::class.simpleName } ?: tagByStacktrace()
        return Logger(tag).also { cachedLogger = it }
    }
}

private fun formatTagFromStacktrace(tag: String): String {
    return tag.substringAfterLast('.').substringBefore("$").let {
        if(it.endsWith("Kt")) {
            it.substringBeforeLast("Kt")
        } else it
    }
}

private fun tagByStacktrace(): String {
    val stacktrace: Array<StackTraceElement> = Exception().stackTrace

    for(element in stacktrace) {
        if(element.className.startsWith(DISALLOWED_CLASS_NAME_PREFIX)) continue
        return formatTagFromStacktrace(element.className)
    }

    return "Awery"
}

/**
 * Creates a [LoggerDelegate] which lazily initializes a [Logger] with the simple name of the class
 * where this function is called.
 */
fun logger() = LoggerDelegate()

/**
 * Creates a [Logger] with the specified tag.
 * @param tag The tag for the logger.
 */
fun logger(tag: String) = Logger(tag)