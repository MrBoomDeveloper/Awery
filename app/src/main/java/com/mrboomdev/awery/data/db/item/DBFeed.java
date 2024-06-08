package com.mrboomdev.awery.data.db.item;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.squareup.moshi.Json;

import java.util.List;

@Entity(tableName = "feed")
public class DBFeed {
	@PrimaryKey
	@NonNull
	public String id;
	public int index;
	public List<CatalogFilter> filters;
	public String tab, title;
	@ColumnInfo(name = "source_manager")
	@Json(name = "source_manager")
	public String sourceManager;
	@ColumnInfo(name = "source_id")
	@Json(name = "source_id")
	public String sourceId;
	@ColumnInfo(name = "source_feed")
	@Json(name = "source_feed")
	public String sourceFeed;
	@ColumnInfo(name = "display_mode")
	@Json(name = "display_mode")
	public DisplayMode displayMode;

	public DBFeed() {
		id = String.valueOf(System.currentTimeMillis());
	}

	public enum DisplayMode {
		LIST_HORIZONTAL,
		LIST_VERTICAL,
		SLIDES,
		GRID
	}
}