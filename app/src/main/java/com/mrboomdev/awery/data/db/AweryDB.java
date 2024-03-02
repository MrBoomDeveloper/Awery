package com.mrboomdev.awery.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.mrboomdev.awery.data.db.dao.CatalogListDao;
import com.mrboomdev.awery.data.db.dao.CatalogMediaDao;

@Database(entities = {DBCatalogMedia.class, DBCatalogList.class}, version = 1, exportSchema = false)
public abstract class AweryDB extends RoomDatabase {

	public abstract CatalogMediaDao getMediaDao();

	public abstract CatalogListDao getListDao();
}