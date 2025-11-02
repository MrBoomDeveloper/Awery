package com.mrboomdev.awery.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "blacklisted_media",
    primaryKeys = ["extension_id", "media_id"]
)
data class DBBlacklistedMedia(
    @ColumnInfo(name = "extension_id")
    val extensionId: String,
    @ColumnInfo(name = "media_id")
    val mediaId: String,
    val name: String
)