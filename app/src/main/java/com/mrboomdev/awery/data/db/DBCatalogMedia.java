package com.mrboomdev.awery.data.db;

import static com.mrboomdev.awery.app.AweryApp.stream;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.util.ParserAdapter;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.Map;

import java9.util.stream.Collectors;

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

	public String titles, ids;
	public String banner, description, extra, country, authors;
	public String duration, type;
	@ColumnInfo(name = "release_date")
	public String releaseDate;
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
	@ColumnInfo(name = "latest_episode")
	public String latestEpisode;
	@ColumnInfo(name = "age_rating")
	public String ageRating;

	public DBCatalogMedia(@NonNull String globalId) {
		this.globalId = globalId;
	}

	@NonNull
	@Contract(pure = true)
	public static DBCatalogMedia fromCatalogMedia(@NonNull CatalogMedia media) {
		var dbMedia = new DBCatalogMedia(media.globalId);
		dbMedia.banner = media.banner;
		dbMedia.description = media.description;
		dbMedia.extra = media.extra;
		dbMedia.country = media.country;
		dbMedia.ageRating = media.ageRating;

		dbMedia.ids = stream(media.ids.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
				.collect(Collectors.joining(",", "{", "}"));

		if(media.averageScore != null) {
			dbMedia.averageScore = Float.toString(media.averageScore);
		}

		if(media.episodesCount != null) {
			dbMedia.episodesCount = Integer.toString(media.episodesCount);
		}

		if(media.authors != null) {
			dbMedia.authors = ParserAdapter.mapToString(media.authors);
		}

		if(media.releaseDate != null) {
			dbMedia.releaseDate = String.valueOf(ParserAdapter.calendarToLong(media.releaseDate));
		}

		if(media.duration != null) {
			dbMedia.duration = Integer.toString(media.duration);
		}

		if(media.latestEpisode != null) {
			dbMedia.latestEpisode = Integer.toString(media.latestEpisode);
		}

		if(media.type != null) {
			dbMedia.type = media.type.name();
		}

		if(media.status != null) {
			dbMedia.status = media.status.name();
		}

		dbMedia.extraLargePoster = media.getBestPoster();
		dbMedia.largePoster = media.poster.large;
		dbMedia.mediumPoster = media.poster.medium;

		if(media.genres != null) {
			dbMedia.genres = StringUtils.listToUniqueString(media.genres);
		}

		if(media.tags != null) {
			dbMedia.tags = ";;;" + stream(media.tags)
					.map(CatalogTag::getName)
					.collect(Collectors.joining(";;;")) + ";;;";
		}

		dbMedia.titles = StringUtils.listToUniqueString(media.titles);
		return dbMedia;
	}

	public CatalogMedia toCatalogMedia() {
		var media = new CatalogMedia(globalId);
		media.banner = banner;
		media.description = description;
		media.extra = extra;
		media.country = country;
		media.ageRating = ageRating;

		try {
			var moshi = new Moshi.Builder().build();
			var type = Types.newParameterizedType(Map.class, String.class, String.class);
			JsonAdapter<Map<String, String>> adapter = moshi.adapter(type);
			media.ids = adapter.fromJson(ids);
		} catch(IOException e) {
			throw new RuntimeException("Failed to parse ids: " + ids);
		}

		if(averageScore != null) {
			media.averageScore = Float.parseFloat(averageScore);
		}

		if(releaseDate != null) {
			var dateLong = Long.parseLong(releaseDate);
			media.releaseDate = ParserAdapter.calendarFromLong(dateLong);
		}

		if(duration != null) {
			media.duration = Integer.parseInt(duration);
		}

		if(episodesCount != null) {
			media.episodesCount = Integer.parseInt(episodesCount);
		}

		if(authors != null) {
			media.authors = ParserAdapter.mapFromString(authors);
		}

		if(latestEpisode != null) {
			media.latestEpisode = Integer.parseInt(latestEpisode);
		}

		media.type = StringUtils.parseEnum(type, CatalogMedia.MediaType.class);
		media.status = StringUtils.parseEnum(status, CatalogMedia.MediaStatus.class);

		media.poster.extraLarge = extraLargePoster;
		media.poster.large = largePoster;
		media.poster.medium = mediumPoster;

		if(genres != null) media.genres = StringUtils.uniqueStringToList(genres);
		if(titles != null) media.titles = StringUtils.uniqueStringToList(titles);

		if(tags != null) {
			media.tags = stream(StringUtils.uniqueStringToList(tags))
					.map(CatalogTag::new).toList();
		}

		return media;
	}
}