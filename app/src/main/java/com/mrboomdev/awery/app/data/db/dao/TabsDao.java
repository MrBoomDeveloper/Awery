package com.mrboomdev.awery.app.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrboomdev.awery.app.data.db.item.DBTab;

import java.util.List;

@Dao
public interface TabsDao {

	@Query("SELECT * from tab")
	List<DBTab> getAllTabs();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(DBTab... tabs);

	@Delete
	void delete(DBTab tab);
}