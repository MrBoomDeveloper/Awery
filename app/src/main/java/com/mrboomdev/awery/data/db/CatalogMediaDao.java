package com.mrboomdev.awery.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

	@Delete
	void delete(DBCatalogMedia catalogMedia);
}