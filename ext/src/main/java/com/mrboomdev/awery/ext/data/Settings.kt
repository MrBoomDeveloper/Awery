package com.mrboomdev.awery.ext.data

import java.util.LinkedList

class Settings: LinkedList<Setting> {
	constructor(items: Collection<Setting>): super(items)
	constructor(vararg items: Setting): super(items.toList())

	operator fun get(key: String, type: Setting.Type? = null): Setting? {
		return find {
			if(type != null) {
				return@find it.key == key && it.type == type
			}

			return@find it.key == key
		}
	}

	fun getRecursively(key: String, type: Setting.Type? = null): Setting? {
		for(item in this) {
			if(type != null) {
				if(item.key == key && item.type == type) {
					return item
				}
			} else {
				if(item.key == key) {
					return item
				}
			}

			item.items?.getRecursively(key, type)?.also {
				return it
			}
		}

		return null
	}
}