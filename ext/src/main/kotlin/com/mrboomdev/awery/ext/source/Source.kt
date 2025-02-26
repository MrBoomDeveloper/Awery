package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.source.module.Module

/**
 * Source is the provider of feeds, media, subtitles and more...
 * An context will be created by the manager,
 * so that you won't need to repeat same values multiple times.
 */
abstract class Source: AbstractSource() {
	override val context: Context.SourceContext
		get() = super.context as Context.SourceContext
	
	abstract fun createModules(): List<Module>
}