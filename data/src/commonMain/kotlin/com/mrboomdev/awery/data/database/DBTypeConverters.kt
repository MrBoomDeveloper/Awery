package com.mrboomdev.awery.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

internal object DBTypeConverters {
    @TypeConverter
    fun serializeStringList(input: List<String>): String {
        return Json.encodeToString(input)
    }

    @TypeConverter
    fun deserializeStringList(input: String): List<String> {
        return Json.decodeFromString(input)
    }
}