package com.mrboomdev.awery.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklisted_keyword")
data class DBBlacklistedKeyword(
    @PrimaryKey
    val name: String
)