package com.mrboomdev.awery.data.db;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mrboomdev.awery.data.db.dao.CatalogListDao;
import com.mrboomdev.awery.data.db.dao.CatalogMediaDao;
import com.mrboomdev.awery.data.db.dao.CatalogMediaProgressDao;
import com.mrboomdev.awery.data.db.dao.RepositoryDao;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.util.ParserAdapter;

@TypeConverters(ParserAdapter.class)
@Database(
		version = 5,

		autoMigrations = {
				@AutoMigration(from = 1, to = 2),
				@AutoMigration(from = 4, to = 5)
		},

		entities = {
				DBCatalogMedia.class,
				DBCatalogList.class,
				DBRepository.class,
				CatalogMediaProgress.class
		}
) public abstract class AweryDB extends RoomDatabase {

	public abstract CatalogMediaDao getMediaDao();

	public abstract CatalogListDao getListDao();

	public abstract RepositoryDao getRepositoryDao();

	public abstract CatalogMediaProgressDao getMediaProgressDao();

	public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase db) {
			db.execSQL("ALTER TABLE `media` ADD COLUMN `ids` TEXT NOT NULL DEFAULT \"{}\"");
			db.execSQL("UPDATE `media` SET `ids` = '{\"anilist\":\"' || `id` || '\"}'");

			db.execSQL("""
				CREATE TABLE media_new (ids TEXT, global_id TEXT NOT NULL PRIMARY KEY, titles TEXT, lists TEXT, trackers TEXT,
				banner TEXT, description TEXT, url TEXT, country TEXT, releaseDate TEXT, duration TEXT, type TEXT,
				episodes_count TEXT, average_score TEXT, tags TEXT, genres TEXT, status TEXT, poster_extra_large TEXT,
				poster_large TEXT, poster_medium TEXT, last_source TEXT, last_episode REAL NOT NULL DEFAULT -1,
				last_episode_progress REAL NOT NULL DEFAULT -1)
			""");

			db.execSQL("""
				INSERT INTO media_new (ids, global_id, titles, lists, trackers, banner, description, url, country, releaseDate, duration, type, episodes_count, average_score, tags, genres, status, status, poster_extra_large, poster_large, poster_medium, last_source, last_episode, last_episode_progress)
								SELECT ids, global_id, titles, lists, trackers, banner, description, url, country, releaseDate, duration, type, episodes_count, average_score, tags, genres, status, status, poster_extra_large, poster_large, poster_medium, last_source, last_episode, last_episode_progress FROM media
			""");

			db.execSQL("DROP TABLE media");
			db.execSQL("ALTER TABLE media_new RENAME TO media");
		}
	};

	public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase db) {
			db.execSQL("""
				CREATE TABLE media_new (ids TEXT, global_id TEXT NOT NULL PRIMARY KEY, titles TEXT, banner TEXT,
				description TEXT, extra TEXT, country TEXT, release_date TEXT, duration TEXT, type TEXT, age_rating TEXT,
				poster_extra_large TEXT, poster_large TEXT, poster_medium TEXT, latest_episode TEXT,
				episodes_count TEXT, average_score TEXT, tags TEXT, genres TEXT, status TEXT, authors TEXT)
			""");

			db.execSQL("""
				CREATE TABLE media_progress (global_id TEXT NOT NULL PRIMARY KEY,
				last_season REAL, last_variant TEXT, last_episode REAL,
				last_watch_source TEXT, last_comments_source TEXT, last_relations_source TEXT,
				lists TEXT, trackers TEXT, progresses TEXT)
			""");

			db.execSQL("""
					INSERT INTO media_new (ids, global_id, titles, banner, poster_extra_large, poster_large, poster_medium, description, country, release_date, duration, type, episodes_count, average_score, tags, genres, status, status)
									SELECT ids, global_id, titles, banner, poster_extra_large, poster_large, poster_medium, description, country, releaseDate, duration, type, episodes_count, average_score, tags, genres, status, status FROM media
			""");

			db.execSQL("""
				INSERT INTO media_progress (global_id, lists)
				SELECT global_id, lists FROM media
			""");

			db.execSQL("DROP TABLE media");
			db.execSQL("ALTER TABLE media_new RENAME TO media");
		}
	};
}