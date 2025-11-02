package com.mrboomdev.awery.data.database.dao

import androidx.room.*
import com.mrboomdev.awery.data.database.entity.DBBlacklistedKeyword

@Dao
interface KeywordBlacklistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(media: DBBlacklistedKeyword)

    @Delete
    suspend fun delete(media: DBBlacklistedKeyword)

    @Query("SELECT * FROM blacklisted_keyword")
    suspend fun getAll(): List<DBBlacklistedKeyword>
}