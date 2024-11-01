package com.mrboomdev.awery.ext.source

abstract class DataSource<T> {
	abstract val data: T
	abstract val size: Long?
}