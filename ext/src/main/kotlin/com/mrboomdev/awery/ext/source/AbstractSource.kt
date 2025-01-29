package com.mrboomdev.awery.ext.source

abstract class AbstractSource: Context {
	abstract val context: Context

	/**
	 * Will be called before unloading this source, so that you can free all your resources.
	 */
	open fun onUnload() {}
}