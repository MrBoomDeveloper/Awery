package com.mrboomdev.awery.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrboomdev.awery.data.db.item.DBCatalogList;

import java.util.List;

@Dao
public interface CatalogListDao {

	@Query("SELECT * FROM list")
	List<DBCatalogList> getAll();

	@Query("SELECT * FROM list WHERE id = :id")
	DBCatalogList get(String id);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(DBCatalogList... lists);

	@Delete
	void delete(DBCatalogList list);
}