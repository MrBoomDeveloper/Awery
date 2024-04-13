package com.mrboomdev.awery.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.mrboomdev.awery.data.db.DBCatalogMedia;

import java.util.List;

@Dao
public interface CatalogMediaDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(DBCatalogMedia... catalogMedia);

	@Update
	void update(DBCatalogMedia... catalogMedia);

	@Query("SELECT * FROM media WHERE titles LIKE :query OR description LIKE :query")
	List<DBCatalogMedia> find(String query);

	@Query("SELECT * FROM media WHERE global_id = :id")
	DBCatalogMedia get(String id);

	@Query("SELECT * FROM media WHERE global_id IN (:ids)")
	List<DBCatalogMedia> getAllByIds(List<String> ids);

	@RawQuery
	List<DBCatalogMedia> getAllByQuery(SupportSQLiteQuery query);

	@Delete
	void delete(DBCatalogMedia catalogMedia);
}