package com.mrboomdev.awery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrboomdev.awery.data.entity.DBList

@Dao
interface ListDao {
	@Query("SELECT * FROM DBList")
	suspend fun getAll(): List<DBList>
	
	@Query("SELECT * FROM DBList WHERE id = :id")
	suspend fun get(id: String): DBList
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(vararg list: DBList)
	
	@Delete
	suspend fun delete(list: DBList)
}