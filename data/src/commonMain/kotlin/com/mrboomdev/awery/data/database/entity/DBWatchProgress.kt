package com.mrboomdev.awery.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.json.Json

/**
 * @param progress Equals to -1 if user just clicked on the watch variant (he didn't even watch it).
 * In that case it is used to mark the *latest* interacted with watch variant like season so
 * that he can remember where he left. Shouldn't be included in the "Continue watching" section if that's the case.
 */
@Entity(primaryKeys = ["extensionId", "mediaId", "variantId"])
data class DBWatchProgress(
    val extensionId: String,
    val mediaId: String,
    val variantId: String,
    val progress: Long,
    val maxProgress: Long? = null,
    val title: String,
    val thumbnail: String? = null
)