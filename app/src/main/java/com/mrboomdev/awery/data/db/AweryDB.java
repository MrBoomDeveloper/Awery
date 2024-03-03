package com.mrboomdev.awery.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.mrboomdev.awery.data.db.dao.CatalogListDao;
import com.mrboomdev.awery.data.db.dao.CatalogMediaDao;

@Database(
		version = 1,

		/*autoMigrations = {
				@AutoMigration(from = 1, to = 2)
		},*/

		entities = {
				DBCatalogMedia.class,
				DBCatalogList.class
		}
) public abstract class AweryDB extends RoomDatabase {

	public abstract CatalogMediaDao getMediaDao();

	public abstract CatalogListDao getListDao();
}