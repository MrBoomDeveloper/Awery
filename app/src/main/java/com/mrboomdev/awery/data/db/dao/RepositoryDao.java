package com.mrboomdev.awery.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.mrboomdev.awery.data.db.DBRepository;

import java.util.List;

@Dao
public interface RepositoryDao {

	@Insert
	void add(DBRepository url);

	@Query("SELECT * from repository WHERE manager = :manager")
	List<DBRepository> getRepositories(String manager);

	@Delete
	void remove(DBRepository url);
}