package com.mrboomdev.awery.data.settings;

import com.squareup.moshi.Json;

public enum SettingsItemType {
	@Json(name = "boolean")
	BOOLEAN,
	@Json(name = "divider")
	DIVIDER,
	@Json(name = "category")
	CATEGORY,
	@Json(name = "screen_boolean")
	SCREEN_BOOLEAN,
	@Json(name = "color")
	COLOR,
	@Json(name = "integer")
	INTEGER,
	@Json(name = "string")
	STRING,
	@Json(name = "screen")
	SCREEN,
	@Json(name = "select")
	SELECT,
	@Json(name = "select_integer")
	SELECT_INTEGER,
	@Json(name = "multiselect")
	MULTISELECT,
	@Json(name = "action")
	ACTION
}