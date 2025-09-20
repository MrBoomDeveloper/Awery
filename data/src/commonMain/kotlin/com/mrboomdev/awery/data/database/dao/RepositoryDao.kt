package com.mrboomdev.awery.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrboomdev.awery.data.database.entity.DBRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(repository: DBRepository)

    @Query("SELECT * FROM DBRepository")
    suspend fun getAll(): List<DBRepository>

    @Query("SELECT * FROM DBRepository")
    fun observeAll(): Flow<List<DBRepository>>

    @Delete
    suspend fun delete(repository: DBRepository)
}