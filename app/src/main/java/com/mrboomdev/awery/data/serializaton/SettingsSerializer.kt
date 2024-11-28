package com.mrboomdev.awery.data.serializaton

import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.data.Settings
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

object SettingsSerializer {
	@FromJson
	fun fromJson(list: List<Setting>): Settings = Settings(list)

	@ToJson
	fun toJson(settings: Settings): List<Setting> = settings
}