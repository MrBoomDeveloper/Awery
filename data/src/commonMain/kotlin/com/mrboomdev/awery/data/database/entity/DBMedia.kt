package com.mrboomdev.awery.data.database.entity

import androidx.room.Entity
import com.mrboomdev.awery.extension.sdk.Media
import kotlinx.serialization.json.Json

@Entity(primaryKeys = ["extensionId", "id"])
data class DBMedia(
    val extensionId: String,
    val id: String,
    val json: String
)

fun DBMedia.toMedia() = Json.decodeFromString<Media>(json)

fun Media.toDBMedia(extensionId: String) = DBMedia(
    extensionId = extensionId,
    id = id,
    json = Json.encodeToString(this)
)