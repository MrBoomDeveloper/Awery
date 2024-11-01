package com.mrboomdev.awery.ext.data

open class Setting(
	var type: Type? = null,
	var value: Any? = null,
	var key: String? = null
) {
	enum class Type {
		STRING,
		INT,
		BOOLEAN,
		SCREEN,
		SCREEN_BOOLEAN,
		ACTION
	}
}