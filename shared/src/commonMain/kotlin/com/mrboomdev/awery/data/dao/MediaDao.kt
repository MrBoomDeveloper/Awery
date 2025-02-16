package com.mrboomdev.awery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrboomdev.awery.data.entity.DBMedia

@Dao
interface MediaDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(media: DBMedia)
	
	@Delete
	suspend fun delete(media: DBMedia)
	
	@Query("SELECT * FROM DBMedia WHERE title LIKE :query OR extras LIKE :query")
	suspend fun find(query: String?): List<DBMedia>
	
	@Query("SELECT * FROM DBMedia WHERE globalId = :id")
	suspend fun get(id: String): DBMedia?
}