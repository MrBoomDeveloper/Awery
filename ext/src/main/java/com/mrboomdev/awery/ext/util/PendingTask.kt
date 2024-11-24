package com.mrboomdev.awery.ext.util

abstract class PendingTask<T> {
	abstract val data: T
	abstract val size: Long?
}