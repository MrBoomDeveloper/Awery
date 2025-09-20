package com.mrboomdev.awery.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mrboomdev.awery.data.database.entity.DBList
import com.mrboomdev.awery.data.database.entity.DBListMediaCrossRef
import com.mrboomdev.awery.data.database.entity.DBMedia
import com.mrboomdev.awery.data.database.entity.toDBMedia
import com.mrboomdev.awery.extension.sdk.Media

@Dao
interface MediaDao {
    @Query("""
        SELECT * FROM DBList
        INNER JOIN DBListMediaCrossRef ON DBList.id = DBListMediaCrossRef.listId
        WHERE DBListMediaCrossRef.mediaExtensionId = :extensionId AND DBListMediaCrossRef.mediaId = :mediaId
    """)
    suspend fun getMediaLists(extensionId: String, mediaId: String): List<DBList>

    @Query("SELECT * FROM DBMedia WHERE extensionId = :extensionId AND id = :id")
    suspend fun get(extensionId: String, id: String): DBMedia?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: DBMedia)
    
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(media: DBMedia)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: DBListMediaCrossRef)

    @Delete
    suspend fun delete(media: DBMedia)
}