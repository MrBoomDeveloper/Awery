package com.mrboomdev.awery.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mrboomdev.awery.data.db.DBCatalogMedia;

import java.util.List;

@Dao
public interface CatalogMediaDao {

	@Query("SELECT * FROM media")
	List<DBCatalogMedia> getAll();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(DBCatalogMedia... catalogMedia);

	@Update
	void update(DBCatalogMedia... catalogMedia);

	@Query("SELECT * FROM media WHERE titles LIKE :query OR description LIKE :query")
	List<DBCatalogMedia> find(String query);

	@Query("SELECT * FROM media WHERE lists LIKE :listId")
	List<DBCatalogMedia> getAllFromList(String listId);

	@Delete
	void delete(DBCatalogMedia catalogMedia);
}