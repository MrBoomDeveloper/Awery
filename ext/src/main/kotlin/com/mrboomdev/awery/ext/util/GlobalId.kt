package com.mrboomdev.awery.ext.util

import com.mrboomdev.awery.ext.source.Source

@Suppress("MemberVisibilityCanBePrivate")
class GlobalId(
	val managerId: String,
	val sourceId: String,
	val itemId: String
) {
	override fun toString() = "$managerId;;;$sourceId;;;$itemId"

	companion object {
		fun parse(globalId: String) = globalId.split(";;;").let {
			// Due to compatibility we have to trim all shit after ":"
			GlobalId(it[0], it[1].split(":")[0], it[2])
		}
	}
}

fun Source.createGlobalId(itemId: String) = "${context.manager.context.id};;;${context.id};;;$itemId"