package com.mrboomdev.awery.app.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.mrboomdev.awery.ext.data.Media;

import java.util.List;

@Dao
public interface MediaDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Media... catalogMedia);

	@Update
	void update(Media... catalogMedia);

	@Query("SELECT * FROM media WHERE titles LIKE :query OR description LIKE :query")
	List<Media> find(String query);

	@Query("SELECT * FROM media WHERE global_id = :id")
	Media get(String id);

	@Query("SELECT * FROM media WHERE global_id IN (:ids)")
	List<Media> getAllByIds(List<String> ids);

	@RawQuery
	List<Media> getAllByQuery(SupportSQLiteQuery query);

	@Delete
	void delete(Media catalogMedia);
}