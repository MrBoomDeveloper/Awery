package com.mrboomdev.awery.ext.source

abstract class AbstractSource {
	private lateinit var baseContext: Context
	
	fun attachContext(context: Context) {
		if(::baseContext.isInitialized) {
			throw IllegalStateException("You cannot attach an Context twice!")
		}
		
		baseContext = context
	}
	
	open val context: Context
		get() = baseContext
	
	/**
	 * Will be called before unloading this source, so that you can free all your resources.
	 */
	open fun onUnload() {}
}