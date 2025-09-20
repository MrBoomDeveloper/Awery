package com.mrboomdev.awery.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrboomdev.awery.data.database.entity.DBWatchProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Dao
abstract class WatchProgressDao {
    @Query("SELECT * FROM DBWatchProgress REVERSE WHERE progress != -1 LIMIT :count")
    abstract fun observeLatest(count: Int): Flow<List<DBWatchProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun add(progress: DBWatchProgress)

    @Query("SELECT * FROM DBWatchProgress WHERE extensionId = :extensionId AND mediaId = :mediaId AND variantId = :variantId")
    abstract suspend fun get(extensionId: String, mediaId: String, variantId: String): DBWatchProgress?

    @Delete
    abstract suspend fun delete(progress: DBWatchProgress)
}