package com.mrboomdev.awery.core.utils

actual object Log {
    actual fun i(tag: String, message: String) {
        println("I/$tag: $message")
    }

    actual fun d(tag: String, message: String) {
        println("D/$tag: $message")
    }

    actual fun w(tag: String, message: String) {
        println("W/$tag: $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        System.err.println("E/$tag: $message\n${throwable?.stackTraceToString()}")
    }
}