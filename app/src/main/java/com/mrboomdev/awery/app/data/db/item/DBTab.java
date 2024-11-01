package com.mrboomdev.awery.app.data.db.item;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.squareup.moshi.Json;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity(tableName = "tab")
public class DBTab implements Comparable<DBTab>, Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	@PrimaryKey
	@NonNull
	public String id;
	public String icon, title;
	public int index;
	@Json(name = "show_end")
	@Ignore
	public boolean showEnd = true;
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