package com.mrboomdev.awery.data.database.dao

import androidx.room.*
import com.mrboomdev.awery.data.database.entity.DBHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(media: DBHistoryItem)

    @Delete
    suspend fun delete(media: DBHistoryItem)

    @Query("SELECT * FROM history_media")
    suspend fun getAll(): List<DBHistoryItem>

    @Query("SELECT * FROM history_media")
    fun observeAll(): Flow<List<DBHistoryItem>>
}