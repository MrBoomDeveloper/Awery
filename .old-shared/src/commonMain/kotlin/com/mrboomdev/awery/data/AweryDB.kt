package com.mrboomdev.awery.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mrboomdev.awery.data.dao.ListDao
import com.mrboomdev.awery.data.dao.MediaDao
import com.mrboomdev.awery.data.dao.MediaProgressDao
import com.mrboomdev.awery.data.dao.RepositoryDao
import com.mrboomdev.awery.data.entity.DBList
import com.mrboomdev.awery.data.entity.DBMedia
import com.mrboomdev.awery.data.entity.DBMediaProgress
import com.mrboomdev.awery.data.entity.DBRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@Database(
	version = 1,
	entities = [
		DBMedia::class,
		DBMediaProgress::class,
		DBRepository::class,
		DBList::class
	]
)
@TypeConverters(AweryDB.Converters::class)
abstract class AweryDB: RoomDatabase() {
	abstract val mediaDao: MediaDao
	abstract val repositoryDao: RepositoryDao
	abstract val mediaProgressDao: MediaProgressDao
	abstract val listDao: ListDao
	
	internal object Converters {
		@TypeConverter
		fun toUniqueString(it: Array<String>) = buildString {
			append(";;;")
			
			for(item in it) {
				if(item.contains(";;;")) {
					throw UnsupportedOperationException("Array item cannot contain \";;;\" in it!")
				}
				
				append(item)
				append(";;;")
			}
		}
		
		@TypeConverter
		fun toArray(it: String): Array<String> {
			if(it.length <= 3) {
				return emptyArray()
			}
			
			return it.substring(3, it.length - 3).split(";;;").toTypedArray()
		}
		
		@TypeConverter
		fun toUniqueString(it: Map<String, String>) = Json.encodeToString(it)
		
		@TypeConverter
		fun toMap(it: String) = Json.decodeFromString<Map<String, String>>(it)
	}
	
	companion object {
		val database by lazy { 
			builder()
				.addMigrations(*migrations)
				.setDriver(BundledSQLiteDriver())
				.setQueryCoroutineContext(Dispatchers.IO)
				.build()
		}
		
		private val migrations = emptyArray<Migration>()
	}
}

internal expect fun AweryDB.Companion.builder(): RoomDatabase.Builder<AweryDB>