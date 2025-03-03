package com.mrboomdev.awery.utils

@JvmName("_createLoggerByStacktrace")
fun createLogger() = createLoggerByStacktrace()

@JvmName("_createLoggerByReflection")
fun Any.createLogger() = this::class.simpleName?.let { className ->
	Logger(className.stripJvmShit())
} ?: createLoggerByStacktrace()

private fun createLoggerByStacktrace(): Logger {
	val stacktrace = Thread.currentThread().stackTrace
	val myClassName = "${Logger::class.qualifiedName}Kt"
	var ourLastElementIndex = 0
	
	stacktrace.forEachIndexed { index, element ->
		if(element.className == myClassName) {
			ourLastElementIndex = index
		}
	}
	
	return Logger(stacktrace[ourLastElementIndex + 1].className.stripJvmShit())
}

/**
 * It'll use parent class name if it is nested, strip package name and the "Kt" suffix.
 */
private fun String.stripJvmShit() = substringAfterLast(".")
	.substringBefore("$")
	.substringBeforeLast("Kt")

expect class Logger(tag: String) {
	fun d(message: String, t: Throwable? = null)
	fun w(message: String, t: Throwable? = null)
	fun e(message: String, t: Throwable? = null)
}