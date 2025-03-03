package com.mrboomdev.awery.utils

import java.util.logging.Level
import java.util.logging.Logger

actual class Logger actual constructor(tag: String) {
	private val logger = Logger.getLogger(tag)
	
	actual fun d(message: String, t: Throwable?) {
		logger.log(Level.FINE, message, t)
	}
	
	actual fun w(message: String, t: Throwable?) {
		logger.log(Level.WARNING, message, t)
	}
	
	actual fun e(message: String, t: Throwable?) {
		logger.log(Level.SEVERE, message, t)
	}
}