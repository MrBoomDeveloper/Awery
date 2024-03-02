package com.mrboomdev.awery.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogTag;
import com.mrboomdev.awery.util.StringUtil;

import org.jetbrains.annotations.Contract;

import java.util.stream.Collectors;

@Entity(tableName = "media")
public class DBCatalogMedia {
	/**
	 * Please use the following format:
	 * <b>EXTENSION_TYPE;EXTENSION_ID;ITEM_ID</b>
	 * <p>
	 * Example:
	 * <b>EXTENSION_JS;;;com.mrboomdev.awery.extension.anilist;;;1</b>
	 * </p>
	 */
	@PrimaryKey
	@ColumnInfo(name = "global_id")
	@NonNull
	public String globalId;

	public String titles, lists, trackers;
	public String title, banner, description, color, url;
	public String type;
	public int id;
	@ColumnInfo(name = "average_score")
	public float averageScore;
	public String tags, genres;
	public String status;
	@ColumnInfo(name = "poster_extra_large")
	public String extraLargePoster;
	@ColumnInfo(name = "poster_large")
	public String largePoster;
	@ColumnInfo(name = "poster_medium")
	public String mediumPoster;

	public DBCatalogMedia(@NonNull String globalId) {
		this.globalId = globalId;
	}

	@NonNull
	@Contract(pure = true)
	public static DBCatalogMedia fromCatalogMedia(@NonNull CatalogMedia media) {
		var dbMedia = new DBCatalogMedia(media.globalId);
		dbMedia.title = media.title;
		dbMedia.banner = media.banner;
		dbMedia.description = media.description;
		dbMedia.color = media.color;
		dbMedia.url = media.url;
		dbMedia.id = media.id;
		dbMedia.averageScore = media.averageScore;

		if(media.type != null) {
			dbMedia.type = media.type.name();
		}

		if(media.status != null) {
			dbMedia.status = media.status.name();
		}

		if(media.trackers != null) {
			dbMedia.trackers = StringUtil.listToUniqueString(media.trackers);
		}

		if(media.lists != null) {
			dbMedia.lists = StringUtil.listToUniqueString(media.lists);
		}

		dbMedia.extraLargePoster = media.getBestPoster();
		dbMedia.largePoster = media.poster.large;
		dbMedia.mediumPoster = media.poster.medium;

		if(media.genres != null) {
			dbMedia.genres = StringUtil.listToUniqueString(media.genres);
		}

		if(media.tags != null) {
			dbMedia.tags = ";;;" + media.tags.stream()
					.map(CatalogTag::getName)
					.collect(Collectors.joining(";;;")) + ";;;";
		}

		dbMedia.titles = StringUtil.listToUniqueString(media.titles);
		return dbMedia;
	}

	public CatalogMedia toCatalogMedia() {
		var media = new CatalogMedia(globalId);
		media.title = title;
		media.banner = banner;
		media.description = description;
		media.status = CatalogMedia.MediaStatus.valueOf(status);
		media.type = CatalogMedia.MediaType.valueOf(type);
		media.id = id;
		media.averageScore = averageScore;
		media.url = url;

		media.poster.extraLarge = extraLargePoster;
		media.poster.large = largePoster;
		media.poster.medium = mediumPoster;

		if(trackers != null) media.trackers = StringUtil.uniqueStringToList(trackers);
		if(lists != null) media.lists = StringUtil.uniqueStringToList(lists);
		if(genres != null) media.genres = StringUtil.uniqueStringToList(genres);
		if(titles != null) media.titles = StringUtil.uniqueStringToList(titles);

		if(tags != null) {
			media.tags = StringUtil.uniqueStringToList(tags)
					.stream().map(CatalogTag::new)
					.collect(Collectors.toList());
		}

		return media;
	}
}