package com.mrboomdev.awery.app.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.mrboomdev.awery.extensions.data.CatalogFeed;

import java.util.List;

@Dao
public interface FeedsDao {

	@Query("SELECT * FROM feed WHERE tab = :tabId")
	List<CatalogFeed> getAllFromTab(String tabId);

	@Insert
	void insert(CatalogFeed feed);

	@Delete
	void delete(CatalogFeed feed);
}