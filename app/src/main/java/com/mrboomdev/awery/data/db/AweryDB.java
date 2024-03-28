package com.mrboomdev.awery.data.db;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mrboomdev.awery.data.db.dao.CatalogListDao;
import com.mrboomdev.awery.data.db.dao.CatalogMediaDao;

@Database(
		version = 3,

		autoMigrations = {
				@AutoMigration(from = 1, to = 2)
		},

		entities = {
				DBCatalogMedia.class,
				DBCatalogList.class
		}
) public abstract class AweryDB extends RoomDatabase {

	public abstract CatalogMediaDao getMediaDao();

	public abstract CatalogListDao getListDao();

	public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase db) {
			db.execSQL("ALTER TABLE `media` ADD COLUMN `ids` TEXT NOT NULL DEFAULT \"{}\"");
			db.execSQL("UPDATE `media` SET `ids` = '{\"anilist\":\"' || `id` || '\"}'");

			db.execSQL("CREATE TABLE media_new (ids TEXT, global_id TEXT NOT NULL PRIMARY KEY, titles TEXT, lists TEXT, trackers TEXT, banner TEXT, description TEXT, url TEXT, country TEXT, releaseDate TEXT, duration TEXT, type TEXT, episodes_count TEXT, average_score TEXT, tags TEXT, genres TEXT, status TEXT, poster_extra_large TEXT, poster_large TEXT, poster_medium TEXT, last_source TEXT, last_episode REAL NOT NULL DEFAULT -1, last_episode_progress REAL NOT NULL DEFAULT -1)");
			db.execSQL("INSERT INTO media_new (ids, global_id, titles, lists, trackers, banner, description, url, country, releaseDate, duration, type, episodes_count, average_score, tags, genres, status, status, poster_extra_large, poster_large, poster_medium, last_source, last_episode, last_episode_progress) SELECT ids, global_id, titles, lists, trackers, banner, description, url, country, releaseDate, duration, type, episodes_count, average_score, tags, genres, status, status, poster_extra_large, poster_large, poster_medium, last_source, last_episode, last_episode_progress FROM media");
			db.execSQL("DROP TABLE media");
			db.execSQL("ALTER TABLE media_new RENAME TO media");
		}
	};
}