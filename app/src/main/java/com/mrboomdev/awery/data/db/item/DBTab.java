package com.mrboomdev.awery.data.db.item;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tab")
public class DBTab implements Comparable<DBTab> {
	@PrimaryKey
	@NonNull
	public String id;
	public String icon, title;
	public int index;

	public DBTab() {
		id = String.valueOf(System.currentTimeMillis());
	}

	@Override
	public int compareTo(@NonNull DBTab o) {
		return Integer.compare(index, o.index);
	}
}