package com.mrboomdev.awery.app.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrboomdev.awery.app.data.db.item.DBCatalogList;

import java.util.List;

@Dao
public interface ListsDao {

	@Query("SELECT * FROM list")
	List<DBCatalogList> getAll();

	@Query("SELECT * FROM list WHERE id = :id")
	DBCatalogList get(String id);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(DBCatalogList... lists);

	@Delete
	void delete(DBCatalogList list);
}