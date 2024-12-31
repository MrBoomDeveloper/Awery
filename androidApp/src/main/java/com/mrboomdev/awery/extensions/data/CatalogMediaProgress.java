package com.mrboomdev.awery.extensions.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "media_progress")
public class CatalogMediaProgress {
	@ColumnInfo(name = "global_id")
	@PrimaryKey
	@NonNull
	public String globalId;
	@ColumnInfo(name = "last_watch_source")
	@Nullable
	public String lastWatchSource;
	@ColumnInfo(name = "last_season")
	public Float lastSeason;
	@ColumnInfo(name = "last_variant")
	@Nullable
	public String lastVariant;
	@ColumnInfo(name = "last_episode")
	public Float lastEpisode;
	@ColumnInfo(name = "last_id")
	@Nullable
	public String lastId;
	@ColumnInfo(name = "last_title")
	@Nullable
	public String lastTitle;
	@ColumnInfo(name = "last_comments_source")
	@Nullable
	public String lastCommentsSource;
	@ColumnInfo(name = "last_relations_source")
	@Nullable
	public String lastRelationsSource;
	@Nullable
	public List<String> lists = new ArrayList<>();
	@Nullable
	public Map<String, String> trackers = new HashMap<>();
	@Nullable
	public Map<Float, Long> progresses = new HashMap<>();

	@NonNull
	public String getGlobalId() {
		return globalId;
	}

	public void clearLists() {
		if(lists != null) lists.clear();
	}

	public boolean isInList(String id) {
		return lists != null && lists.contains(id);
	}

	public int getListsCount() {
		return lists == null ? 0 : lists.size();
	}

	public void addToList(String id) {
		if(lists == null) lists = new ArrayList<>();
		lists.add(id);
	}

	public CatalogMediaProgress(@NonNull String globalId) {
		this.globalId = globalId;
	}
}