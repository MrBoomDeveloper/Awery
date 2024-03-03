package com.mrboomdev.awery.data.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogTag;
import com.mrboomdev.awery.util.StringUtil;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
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
	public String title, banner, description, color, url, country;
	public String releaseDate, duration, type;
	public int id;
	@ColumnInfo(name = "episodes_count")
	public String episodesCount;
	@ColumnInfo(name = "average_score")
	public String averageScore;
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
		dbMedia.country = media.country;

		if(media.averageScore != null) {
			dbMedia.averageScore = Float.toString(media.averageScore);
		}

		if(media.episodesCount != null) {
			dbMedia.episodesCount = Integer.toString(media.episodesCount);
		}

		if(media.releaseDate != null) {
			var stringDate = CatalogMedia.adapter.toJson(media.releaseDate);
			dbMedia.releaseDate = Long.toString(stringDate);
		}

		if(media.duration != null) {
			dbMedia.duration = Integer.toString(media.duration);
		}

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
		media.id = id;
		media.url = url;
		media.color = color;
		media.country = country;

		if(averageScore != null) {
			media.averageScore = Float.parseFloat(averageScore);
		}

		if(releaseDate != null) {
			var dateLong = Long.parseLong(releaseDate);
			media.releaseDate = CatalogMedia.adapter.fromJson(dateLong);
		}

		if(duration != null) {
			media.duration = Integer.parseInt(duration);
		}

		if(episodesCount != null) {
			media.episodesCount = Integer.parseInt(episodesCount);
		}

		media.type = StringUtil.parseEnum(type, CatalogMedia.MediaType.class);
		media.status = StringUtil.parseEnum(status, CatalogMedia.MediaStatus.class);

		media.poster.extraLarge = extraLargePoster;
		media.poster.large = largePoster;
		media.poster.medium = mediumPoster;

		if(trackers != null) media.trackers = StringUtil.uniqueStringToList(trackers);
		if(lists != null) media.lists = new ArrayList<>(StringUtil.uniqueStringToList(lists));
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