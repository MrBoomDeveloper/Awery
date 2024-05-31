package com.mrboomdev.awery.data.db.item;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.sdk.data.CatalogFilter;

import java.util.List;

@Entity(tableName = "feed")
public class DBFeed {
	@PrimaryKey
	@NonNull
	public String id;
	public List<CatalogFilter> filters;
	public String tab, title;
	@ColumnInfo(name = "source_manager")
	public String sourceManager;
	@ColumnInfo(name = "source_id")
	public String sourceId;
	@ColumnInfo(name = "source_feed")
	public String sourceFeed;

	public DBFeed() {
		id = String.valueOf(System.currentTimeMillis());
	}
}