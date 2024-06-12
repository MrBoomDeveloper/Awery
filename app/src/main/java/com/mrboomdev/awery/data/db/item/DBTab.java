package com.mrboomdev.awery.data.db.item;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.extensions.data.CatalogFeed;

import java.util.List;

@Entity(tableName = "tab")
public class DBTab implements Comparable<DBTab> {
	@PrimaryKey
	@NonNull
	public String id;
	public String icon, title;
	public int index;
	@Ignore
	public List<CatalogFeed> feeds;

	public DBTab() {
		id = String.valueOf(System.currentTimeMillis());
	}

	@Override
	public int compareTo(@NonNull DBTab o) {
		return Integer.compare(index, o.index);
	}
}