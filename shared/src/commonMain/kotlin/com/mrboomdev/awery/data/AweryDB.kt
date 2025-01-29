package com.mrboomdev.awery.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.mrboomdev.awery.data.dao.MediaDao
import com.mrboomdev.awery.data.dao.RepositoryDao
import com.mrboomdev.awery.data.entity.DBMedia
import com.mrboomdev.awery.data.entity.asDBMedia
import com.mrboomdev.awery.ext.data.CatalogMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@TypeConverters(AweryDB.Converters::class)
@Database(
	version = 5,
	
	entities = [
		DBMedia::class
	],
	
	autoMigrations = [
		AutoMigration(from = 1, to = 2),
		AutoMigration(from = 4, to = 5)
	]
)
abstract class AweryDB: RoomDatabase() {
	abstract fun getMediaDao(): MediaDao
	abstract fun getRepositoryDao(): RepositoryDao
	
	internal object Converters {
		@TypeConverter
		fun CatalogMedia.toDBMedia() = asDBMedia()
		
		@TypeConverter
		fun DBMedia.toCatalogMedia() = asCatalogMedia()
		
		@TypeConverter
		fun Array<String>.toUniqueString() = buildString {
			append(";;;")
			
			for(item in this@toUniqueString) {
				if(item.contains(";;;")) {
					throw UnsupportedOperationException("Array item cannot contain \";;;\" in it!")
				}
				
				append(item)
				append(";;;")
			}
		}
		
		@TypeConverter
		fun String.toArray(): Array<String> {
			if(length <= 3) {
				return emptyArray()
			}
			
			return substring(3, length - 3).split(";;;").toTypedArray()
		}
		
		@TypeConverter
		fun Map<String, String>.toUniqueString() = Json.encodeToString(this)
		
		@TypeConverter
		fun String.toMap() = Json.decodeFromString<Map<String, String>>(this)
	}
	
	companion object {
		val instance by lazy { 
			builder()
				.addMigrations(*migrations)
				.setDriver(BundledSQLiteDriver())
				.setQueryCoroutineContext(Dispatchers.IO)
				.build()
		}
		
		private val migrations = arrayOf(
			object : Migration(2, 3) {
				override fun migrate(connection: SQLiteConnection) {
					connection.execSQL("ALTER TABLE `media` ADD COLUMN `ids` TEXT NOT NULL DEFAULT \"{}\"")
					connection.execSQL("UPDATE `media` SET `ids` = '{\"anilist\":\"' || `id` || '\"}'")
					
					connection.execSQL("""
						CREATE TABLE media_new (ids TEXT, global_id TEXT NOT NULL PRIMARY KEY, titles TEXT, lists TEXT, trackers TEXT,
							banner TEXT, description TEXT, url TEXT, country TEXT, releaseDate TEXT, duration TEXT, type TEXT,
							episodes_count TEXT, average_score TEXT, tags TEXT, genres TEXT, status TEXT, poster_extra_large TEXT,
							poster_large TEXT, poster_medium TEXT, last_source TEXT, last_episode REAL NOT NULL DEFAULT -1,
							last_episode_progress REAL NOT NULL DEFAULT -1)
					""".trimIndent())
					
					connection.execSQL("""
						INSERT INTO media_new (ids, global_id, titles, lists, trackers, banner, description, url, country, releaseDate, 
							duration, type, episodes_count, average_score, tags, genres, status, status, poster_extra_large, poster_large, 
							poster_medium, last_source, last_episode, last_episode_progress)
							
						SELECT ids, global_id, titles, lists, trackers, banner, description, url, country, releaseDate, duration, type, 
							episodes_count, average_score, tags, genres, status, status, poster_extra_large, poster_large, poster_medium, 
							last_source, last_episode, last_episode_progress FROM media
					""".trimIndent())
					
					connection.execSQL("DROP TABLE media")
					connection.execSQL("ALTER TABLE media_new RENAME TO media")
				}
			},
			
			object : Migration(3, 4) {
				override fun migrate(connection: SQLiteConnection) {
					connection.execSQL("""
						CREATE TABLE media_new (ids TEXT, global_id TEXT NOT NULL PRIMARY KEY, titles TEXT, banner TEXT,
							description TEXT, extra TEXT, country TEXT, release_date TEXT, duration TEXT, type TEXT, age_rating TEXT,
							poster_extra_large TEXT, poster_large TEXT, poster_medium TEXT, latest_episode TEXT,
							episodes_count TEXT, average_score TEXT, tags TEXT, genres TEXT, status TEXT, authors TEXT)
					""".trimIndent())
					
					connection.execSQL("""
						CREATE TABLE media_progress (global_id TEXT NOT NULL PRIMARY KEY, last_season REAL, last_variant TEXT, 
							last_episode REAL, last_watch_source TEXT, last_comments_source TEXT, last_relations_source TEXT,
							lists TEXT, trackers TEXT, progresses TEXT)
					""".trimIndent())
					
					connection.execSQL("""
						INSERT INTO media_new (ids, global_id, titles, banner, poster_extra_large, poster_large, poster_medium, 
							description, country, release_date, duration, type, episodes_count, average_score, tags, genres, status)
							
						SELECT ids, global_id, titles, banner, poster_extra_large, poster_large, poster_medium, description, country, 
							releaseDate, duration, type, episodes_count, average_score, tags, genres, status FROM media
					""".trimIndent())
					
					connection.execSQL("""
						INSERT INTO media_progress (global_id, lists)
						SELECT global_id, lists FROM media
					""".trimIndent())
					
					connection.execSQL("DROP TABLE media")
					connection.execSQL("ALTER TABLE media_new RENAME TO media")
				}
			}
		)
	}
}

internal expect fun AweryDB.Companion.builder(): RoomDatabase.Builder<AweryDB>