package com.mrboomdev.awery.ext.data

import com.mrboomdev.awery.ext.util.Image

open class Setting {
	open val type: Type? = null
	open val key: String? = null
	open val title: String? = null
	open val description: String? = null
	open val items: Settings? = null
	open var value: Any? = null
	open val icon: Image? = null
	open val isVisible: Boolean = true

	open fun onClick() {}

	enum class Type {
		STRING,
		INT,
		FLOAT,
		BOOLEAN,
		SCREEN,
		SCREEN_BOOLEAN,
		ACTION,
		TRI_STATE
	}

	enum class TriState {
		CHECKED, UNCHECKED, EMPTY
	}
}