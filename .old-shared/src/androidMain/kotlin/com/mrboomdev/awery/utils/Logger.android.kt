package com.mrboomdev.awery.utils

import android.util.Log

actual class Logger actual constructor(private val tag: String) {
	actual fun d(message: String, t: Throwable?) { 
		Log.d(tag, message, t)
	}
	
	actual fun w(message: String, t: Throwable?) {
		Log.w(tag, message, t)
	}
	
	actual fun e(message: String, t: Throwable?) {
		Log.e(tag, message, t)
	}
}