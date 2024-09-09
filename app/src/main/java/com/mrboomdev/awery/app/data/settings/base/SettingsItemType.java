package com.mrboomdev.awery.app.data.settings.base;

import com.squareup.moshi.Json;

@Deprecated(forRemoval = true)
public enum SettingsItemType {
	@Json(name = "boolean")
	BOOLEAN {
		@Override
		public boolean isBoolean() {
			return true;
		}
	},

	@Json(name = "screen_boolean")
	SCREEN_BOOLEAN {
		@Override
		public boolean isBoolean() {
			return true;
		}
	},

	@Json(name = "json")
	JSON,
	@Json(name = "serializable")
	SERIALIZABLE,
	@Json(name = "date")
	DATE,
	@Json(name = "excludable")
	EXCLUDABLE,
	@Json(name = "divider")
	DIVIDER,
	@Json(name = "category")
	CATEGORY,
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
	ACTION;

	public boolean isBoolean() {
		return false;
	}
}