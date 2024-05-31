package com.mrboomdev.awery.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.mrboomdev.awery.data.db.item.DBFeed;

import java.util.List;

@Dao
public interface FeedsDao {

	@Query("SELECT * FROM feed WHERE tab = :tabId")
	List<DBFeed> getAllFromTab(String tabId);

	@Insert
	void insert(DBFeed feed);

	@Delete
	void delete(DBFeed feed);
}