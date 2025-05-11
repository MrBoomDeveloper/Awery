package com.mrboomdev.awery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrboomdev.awery.data.entity.DBMediaProgress

@Dao
interface MediaProgressDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(progress: DBMediaProgress)
	
	@Query("SELECT * FROM DBMediaProgress WHERE globalId = :globalId")
	suspend fun get(globalId: String): DBMediaProgress
	
	@Query("SELECT * FROM DBMediaProgress WHERE lists LIKE '%;;;' || :list || ';;;%'")
	suspend fun getAllFromList(list: String): List<DBMediaProgress>
}