package com.mrboomdev.awery.ui.routes

import cafe.adriel.voyager.core.screen.Screen
import com.mrboomdev.awery.ui.utils.UniqueIdGenerator
import kotlinx.serialization.Serializable

/**
 * Uses an unique key for every instance, so no crashes should happen 😅
 */
@Serializable
abstract class BaseRoute: Screen {
	override val key = uniqueIdGenerator.long.toString()
	
	companion object {
		private val uniqueIdGenerator = UniqueIdGenerator(
			initialValue = Long.MIN_VALUE,
			overflowMode = UniqueIdGenerator.OverflowMode.RESET
		)
	}
}