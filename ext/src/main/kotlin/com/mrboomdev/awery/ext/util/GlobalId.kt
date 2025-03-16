package com.mrboomdev.awery.ext.util

import com.mrboomdev.awery.ext.source.Source
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class GlobalId(val value: String) {
	
	constructor(
		managerId: String,
		sourceId: String,
		itemId: String
	): this("$managerId;;;$sourceId;;;$itemId")
	
	constructor(
		managerId: String,
		sourceId: String
	): this("$managerId;;;$sourceId")
	
	val managerId get() = parse(value)[0]
	val sourceId get() = parse(value).getOrNull(1)
	val itemId get() = parse(value).getOrNull(2)
	
	override fun toString() = buildString { 
		append("{ \"manager_id\": \"")
		append(managerId)
		append("\"")
		
		sourceId?.let {
			append(", \"source_id\": \"")
			append(it)
			append("\"")
		}
		
		itemId?.let {
			append(", \"item_id\": \"")
			append(it)
			append("\"")
		}
		
		append(" }")
	}

	companion object {
		fun parse(globalId: String) = globalId.split(";;;").let {
			// Due to compatibility we have to trim all shit after ":".
			// Also there may be less than 3 args, so we get values by an "safe" way.
			arrayOf(it[0], it.getOrNull(1)?.split(":")?.get(0), it.getOrNull(2)).filterNotNull()
		}
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun Source.createGlobalId(itemId: String): GlobalId {
	return GlobalId(context.manager.context.id, context.id, itemId)
}
