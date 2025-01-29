package com.mrboomdev.awery.data.settings

import com.mrboomdev.awery.ext.data.Setting

operator fun List<Setting>.get(key: String, type: Setting.Type? = null): Setting? {
	return find {
		if(type != null) {
			return@find it.key == key && it.type == type
		}
		
		return@find it.key == key
	}
}

fun List<Setting>.getRecursively(key: String, type: Setting.Type? = null): Setting? {
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