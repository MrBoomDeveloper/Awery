package com.mrboomdev.awery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.mrboomdev.awery.ext.data.CatalogMedia

@Dao
interface MediaDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(media: CatalogMedia)
	
	@Delete
	suspend fun delete(media: CatalogMedia)
}