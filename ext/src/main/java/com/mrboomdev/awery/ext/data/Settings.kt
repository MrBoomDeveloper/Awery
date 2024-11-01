package com.mrboomdev.awery.ext.data

import java.util.LinkedList

class Settings(vararg settings: Setting) : LinkedList<Setting>(settings.toList()) {

	operator fun get(key: String): Setting? {
		return find {
			it.key == key
		}
	}

	fun get(key: String, type: Setting.Type): Setting? {
		return find {
			it.key == key && it.type == type
		}
	}
}