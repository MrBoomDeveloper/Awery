package com.mrboomdev.awery.data.database.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(primaryKeys = ["extensionId", "url"])
@Serializable
data class DBRepository(
    val extensionId: String,
    val name: String,
    val url: String
)