package com.mrboomdev.awery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrboomdev.awery.data.entity.DBRepository

@Dao
interface RepositoryDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(repository: DBRepository)

	@Delete
	suspend fun delete(repository: DBRepository)
	
	@Query("SELECT * FROM DBRepository WHERE manager = :manager")
	suspend fun fromManager(manager: String): List<DBRepository>
}