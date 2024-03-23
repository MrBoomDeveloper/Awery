package com.mrboomdev.awery.data.settings;

import com.squareup.moshi.Json;

public enum SettingsItemType {
	@Json(name = "boolean")
	BOOLEAN,
	@Json(name = "screen_boolean")
	SCREEN_BOOLEAN,
	@Json(name = "int")
	INT,
	@Json(name = "string")
	STRING,
	@Json(name = "screen")
	SCREEN,
	@Json(name = "select")
	SELECT,
	@Json(name = "multiselect")
	MULTISELECT,
	@Json(name = "action")
	ACTION
}