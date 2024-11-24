package com.mrboomdev.awery.data.db.item;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "repository")
public class DBRepository {
	@PrimaryKey
	@NonNull
	public String url;
	@NonNull
	public String manager;
	@ColumnInfo(name = "is_enabled", defaultValue = "true")
	public boolean isEnabled = true;

	public DBRepository(@NonNull String url, @NonNull String manager) {
		this.url = url;
		this.manager = manager;
	}
}