package com.mrboomdev.awery.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogTag;

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

	public String titles;
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
		dbMedia.type = media.type.name();
		dbMedia.id = media.id;
		dbMedia.averageScore = media.averageScore;
		dbMedia.status = media.status.name();

		dbMedia.extraLargePoster = media.poster.extraLarge;
		dbMedia.largePoster = media.poster.large;
		dbMedia.mediumPoster = media.poster.medium;

		if(media.genres != null) {
			dbMedia.genres = String.join(";;;", media.genres);
		}

		if(media.tags != null) {
			dbMedia.tags = media.tags.stream()
					.map(CatalogTag::getName)
					.collect(Collectors.joining(";;;"));
		}

		dbMedia.titles = String.join(";;;", media.titles);

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
		return media;
	}
}