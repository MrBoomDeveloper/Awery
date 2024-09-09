package com.mrboomdev.awery.app.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;

import java.util.List;

@Dao
public interface MediaProgressDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(CatalogMediaProgress progress);

	@Query("SELECT * FROM media_progress WHERE global_id = :globalId")
	CatalogMediaProgress get(String globalId);

	@Query("SELECT * FROM media_progress WHERE lists LIKE '%;;;' || :list || ';;;%'")
	List<CatalogMediaProgress> getAllFromList(String list);
}