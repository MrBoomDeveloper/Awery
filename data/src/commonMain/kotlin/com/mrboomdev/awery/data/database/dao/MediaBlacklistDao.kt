package com.mrboomdev.awery.data.database.dao

import androidx.room.*
import com.mrboomdev.awery.data.database.entity.DBBlacklistedMedia

@Dao
interface MediaBlacklistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(media: DBBlacklistedMedia)
    
    @Delete
    suspend fun delete(media: DBBlacklistedMedia)
    
    @Query("SELECT EXISTS(SELECT 1 FROM blacklisted_media WHERE extension_id = :extensionId AND media_id = :mediaId)")
    suspend fun isBlacklisted(extensionId: String, mediaId: String): Boolean
    
    @Query("SELECT * FROM blacklisted_media")
    suspend fun getAll(): List<DBBlacklistedMedia>
}