package com.mrboomdev.awery.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DBCatalogMedia.class}, version = 1)
public abstract class AweryDB extends RoomDatabase {

	public abstract CatalogMediaDao getMediaDao();
}