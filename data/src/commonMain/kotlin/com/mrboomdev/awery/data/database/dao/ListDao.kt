package com.mrboomdev.awery.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mrboomdev.awery.data.database.entity.DBList
import com.mrboomdev.awery.data.database.entity.DBMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    @Query("""
        SELECT * FROM DBMedia
        INNER JOIN DBListMediaCrossRef ON DBMedia.extensionId = DBListMediaCrossRef.mediaExtensionId AND DBMedia.id = DBListMediaCrossRef.mediaId
        WHERE DBListMediaCrossRef.listId = :listId
        ORDER BY DBMedia.rowid DESC
    """)
    fun observeMediaInList(listId: Long): Flow<List<DBMedia>>

    @Query("SELECT * FROM DBList")
    fun observeAll(): Flow<List<DBList>>
    
    @Query("SELECT COUNT(*) FROM DBList")
    fun observeCount(): Flow<Int>

    @Transaction
    suspend fun updateLists(collection: Collection<DBList>) {
        collection.forEach { insert(it) }
    }

    @Query("SELECT * FROM DBList")
    suspend fun getAll(): List<DBList>

    @Query("SELECT * FROM DBList WHERE id = :id")
    suspend operator fun get(id: Long): DBList

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: DBList): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(list: DBList)

    @Delete
    suspend fun delete(list: DBList)
}