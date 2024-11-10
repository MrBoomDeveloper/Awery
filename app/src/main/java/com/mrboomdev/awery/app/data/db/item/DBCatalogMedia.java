package com.mrboomdev.awery.app.data.db.item;

import static com.mrboomdev.awery.util.NiceUtils.listToUniqueString;
import static com.mrboomdev.awery.util.NiceUtils.parseEnum;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.NiceUtils.uniqueStringToList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.ext.constants.AweryAgeRating;
import com.mrboomdev.awery.ext.data.CatalogMedia;
import com.mrboomdev.awery.ext.data.CatalogTag;
import com.mrboomdev.awery.util.ParserAdapter;

import org.jetbrains.annotations.Contract;

import java.util.List;

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
	@Nullable
	public String titles, description, ids, url, banner, extra, country, authors, duration, type;
	@ColumnInfo(name = "release_date")
	@Nullable
	public String releaseDate;
	@ColumnInfo(name = "episodes_count")
	@Nullable
	public String episodesCount;
	@ColumnInfo(name = "average_score")
	@Nullable
	public String averageScore;
	@Nullable
	public String tags, genres;
	@Nullable
	public String status;
	@ColumnInfo(name = "poster_extra_large")
	@Nullable
	public String poster;
	@ColumnInfo(name = "poster_large")
	@Deprecated(forRemoval = true)
	@Nullable
	public String __DEPRECATED_largePoster;
	@ColumnInfo(name = "poster_medium")
	@Deprecated(forRemoval = true)
	@Nullable
	public String __DEPRECATED_mediumPoster;
	@ColumnInfo(name = "latest_episode")
	@Nullable
	public String latestEpisode;
	@ColumnInfo(name = "age_rating")
	@Nullable
	public String ageRating;

	public DBCatalogMedia(@NonNull String globalId) {
		this.globalId = globalId;
	}

	@NonNull
	@Contract(pure = true)
	public static DBCatalogMedia fromCatalogMedia(@NonNull CatalogMedia media) {
		var dbMedia = new DBCatalogMedia(media.getGlobalId());
		dbMedia.banner = media.getBanner();
		dbMedia.description = media.getDescription();
		dbMedia.extra = media.getExtra();
		dbMedia.country = media.getCountry();
		dbMedia.ageRating = String.valueOf(media.getAgeRating());
		dbMedia.url = media.getUrl();

		if(media.getIds() != null) {
			dbMedia.ids = stream(media.getIds().entrySet())
					.map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
					.collect(Collectors.joining(",", "{", "}"));
		}
		
		if(media.getAverageScore() != null) {
			dbMedia.averageScore = Float.toString(media.getAverageScore());
		}

		if(media.getEpisodesCount() != null) {
			dbMedia.episodesCount = Integer.toString(media.getEpisodesCount());
		}

		if(media.getAuthors() != null) {
			dbMedia.authors = ParserAdapter.mapToString(media.getAuthors());
		}

		if(media.getReleaseDate() != null) {
			dbMedia.releaseDate = String.valueOf(media.getReleaseDate());
		}

		if(media.getDuration() != null) {
			dbMedia.duration = Integer.toString(media.getDuration());
		}

		if(media.getLatestEpisode() != null) {
			dbMedia.latestEpisode = Integer.toString(media.getLatestEpisode());
		}

		if(media.getType() != null) {
			dbMedia.type = media.getType().name();
		}

		if(media.getStatus() != null) {
			dbMedia.status = media.getStatus().name();
		}

		if(media.getPoster() != null) {
			dbMedia.poster = media.getPoster();
		}

		if(media.getGenres() != null) {
			dbMedia.genres = listToUniqueString(List.of(media.getGenres()));
		}

		if(media.getTags() != null) {
			dbMedia.tags = ";;;" + stream(media.getTags())
					.map(CatalogTag::getName)
					.collect(Collectors.joining(";;;")) + ";;;";
		}

		if(media.getTitles() != null) {
			dbMedia.titles = listToUniqueString(List.of(media.getTitles()));
		}

		return dbMedia;
	}

	public CatalogMedia toCatalogMedia() {
		return new CatalogMedia(
				globalId,
				banner,
				description,
				country,
				parseEnum(ageRating, AweryAgeRating.class),
				extra,
				url,
				parseEnum(type, CatalogMedia.Type.class),
				poster,
				releaseDate != null ? Long.parseLong(releaseDate) : null,
				duration != null ? Integer.parseInt(duration) : null,
				episodesCount != null ? Integer.parseInt(episodesCount) : null,
				latestEpisode != null ? Integer.parseInt(latestEpisode) : null,
				averageScore != null ? Float.parseFloat(averageScore) : null,
				parseEnum(status, CatalogMedia.Status.class),
				
				tags != null ? stream(uniqueStringToList(tags))
						.map(CatalogTag::new).toArray(CatalogTag[]::new) : null,
				
				genres == null ? null : stream(uniqueStringToList(genres)).toArray(String[]::new),
				titles == null ? null : stream(uniqueStringToList(titles)).toArray(String[]::new),
				authors == null ? null : ParserAdapter.mapFromString(authors),
				ids == null ? null : ParserAdapter.mapFromString(ids));
	}
}