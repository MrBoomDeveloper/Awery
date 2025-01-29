package com.mrboomdev.awery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrboomdev.awery.ext.data.CatalogMedia

@Dao
interface MediaDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(media: CatalogMedia)
	
	@Delete
	suspend fun delete(media: CatalogMedia)
	
	@Query("SELECT * FROM DBMedia WHERE titles LIKE :query OR description LIKE :query")
	suspend fun find(query: String?): List<CatalogMedia?>
	
	@Query("SELECT * FROM DBMedia WHERE globalId = :id")
	suspend fun get(id: String): CatalogMedia?
}