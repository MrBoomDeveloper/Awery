package com.mrboomdev.awery.data.database

import androidx.room.TypeConverter
import com.mrboomdev.awery.extension.sdk.Media
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
    
    @TypeConverter
    fun serializeMedia(input: Media): String {
        return Json.encodeToString(input)
    }
    
    @TypeConverter
    fun deserializeMedia(input: String): Media {
        return Json.decodeFromString(input)
    }
}