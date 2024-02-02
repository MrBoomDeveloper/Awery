package com.mrboomdev.awery.data.settings;

import com.squareup.moshi.Json;

public enum SettingsItemType {
	@Json(name = "boolean")
	BOOLEAN,
	@Json(name = "screen")
	SCREEN,
	@Json(name = "select")
	SELECT
}