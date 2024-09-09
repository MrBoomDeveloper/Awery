package com.mrboomdev.awery.app.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrboomdev.awery.ext.source.Repository;

import java.util.List;

@Dao
public interface RepositoryDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void add(Repository url);

	@Query("SELECT * from repository WHERE manager = :manager")
	List<Repository> getRepositories(String manager);

	@Delete
	void remove(Repository url);
}