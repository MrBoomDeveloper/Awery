package com.mrboomdev.awery.ext.data

import com.mrboomdev.awery.ext.util.Image

open class Setting {
	open val type: Type? = null
	open val key: String? = null
	open val title: String? = null
	open val description: String? = null
	open val items: List<Setting>? = null
	open val from: Float? = null
	open val to: Float? = null
	open var value: Any? = null
	open val icon: Image? = null
	open val isVisible: Boolean = true

	open fun onClick() {}

	enum class Type {
		STRING,
		INTEGER,
		FLOAT,
		BOOLEAN,
		SCREEN,
		SCREEN_BOOLEAN,
		SELECT,
		MULTISELECT,
		ACTION,
		TRI_STATE,
		CATEGORY;

		val isBoolean: Boolean
			get() = this == BOOLEAN || this == SCREEN_BOOLEAN

		val isScreen: Boolean
			get() = this == SCREEN || this == SCREEN_BOOLEAN
	}

	enum class TriState {
		CHECKED, UNCHECKED, EMPTY
	}

	override fun equals(other: Any?): Boolean {
		return other is Setting
				&& type == other.type
				&& key == other.key
				&& title == other.title
				&& description == other.description
				&& items == other.items
				&& from == other.from
				&& to == other.to
				&& icon == other.icon
				&& value == other.value
				&& isVisible == other.isVisible
	}

	override fun hashCode(): Int {
		var result = type?.hashCode() ?: 0
		result = 31 * result + (key?.hashCode() ?: 0)
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (items?.hashCode() ?: 0)
		result = 31 * result + (from?.hashCode() ?: 0)
		result = 31 * result + (to?.hashCode() ?: 0)
		result = 31 * result + (value?.hashCode() ?: 0)
		result = 31 * result + (icon?.hashCode() ?: 0)
		result = 31 * result + isVisible.hashCode()
		return result
	}
}

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