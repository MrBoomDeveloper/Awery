package com.mrboomdev.awery.data.db.item;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tab")
public class DBTab {
	@PrimaryKey
	@NonNull
	public String id;
	public String icon, title;

	public DBTab() {
		id = String.valueOf(System.currentTimeMillis());
	}
}