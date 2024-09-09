package com.mrboomdev.awery.ext.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.mrboomdev.awery.ext.constants.Awery;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "media")
public final class Media implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private static JsonAdapter<Media> adapter;
	@NotNull
	@ColumnInfo(name = "global_id")
	@Json(name = "global_id")
	private final String globalId;
	@NotNull
	private final Type type;
	private Map<String, String> ids;
	private Map<ImageType, String> images;
	@ColumnInfo(name = "external_services")
	@Json(name = "external_services")
	private ExternalService[] externalServices;
	private String[] titles, genres, tags, flags;
	private User[] authors;
	@ColumnInfo(name = "age_rating")
	@Json(name = "age_rating")
	private String ageRating;
	private String description, extra, country;
	@ColumnInfo(name = "release_date")
	@Json(name = "release_date")
	private Long releaseDate;
	private Long duration;
	private Integer episodes;
	@ColumnInfo(name = "latest_episode")
	public Integer latestEpisode;
	private Float score;
	private Status status;

	/**
	 * @param globalId The unique id of the media in the following format:
	 * <p>{@code MANAGER_ID;;;PROVIDER_ID:EXTENSION_ID;;;ITEM_ID}</p>
	 */
	public Media(@NotNull Type type, @NotNull String globalId) {
		this.type = type;
		this.globalId = globalId;
	}

	public Media(
			@NotNull Type type,
			@NotNull String managerId,
			@NotNull String extensionId,
			@NotNull String providerId,
			@NotNull String localId
	) {
		this(type, managerId + ";;;" + providerId + ":" + extensionId + ";;;" + localId);
	}

	private Media(@NotNull Media original) {
		type = original.type;
		globalId = original.globalId;

		score = original.score;
		latestEpisode = original.latestEpisode;
		status = original.status;
		episodes = original.episodes;
		duration = original.duration;
		releaseDate = original.releaseDate;
		ageRating = original.ageRating;
		description = original.description;
		extra = original.extra;
		country = original.country;

		if(original.genres != null) {
			genres = Arrays.copyOf(original.genres, original.genres.length);
		}

		if(original.authors != null) {
			authors = Arrays.copyOf(original.authors, original.authors.length);
		}

		if(original.externalServices != null) {
			externalServices = Arrays.copyOf(original.externalServices, original.externalServices.length);
		}

		if(original.titles != null) {
			titles = Arrays.copyOf(original.titles, original.titles.length);
		}

		if(original.genres != null) {
			genres = Arrays.copyOf(original.genres, original.genres.length);
		}

		if(original.tags != null) {
			tags = Arrays.copyOf(original.tags, original.tags.length);
		}

		if(original.images != null) {
			images = new HashMap<>(original.images);
		}

		if(original.ids != null) {
			ids = new HashMap<>(original.ids);
		}

		if(original.flags != null) {
			flags = Arrays.copyOf(original.flags, original.flags.length);
		}
	}

	public enum Type {
		MOVIE, TV, COMICS, BOOK;

		public boolean canRead() {
			return this == BOOK || this == COMICS;
		}
	}

	/**
	 * @return The unique id of the media in the following format:
	 * <p>{@code MANAGER_ID;;;PROVIDER_ID:EXTENSION_ID;;;ITEM_ID}</p>
	 */
	@NotNull
	public String getGlobalId() {
		return globalId;
	}

	@Nullable
	public Integer getLatestEpisode() {
		return latestEpisode;
	}

	@NotNull
	public Type getType() {
		return type;
	}

	@Nullable
	public String[] getScreenshots() {
		var images = getImage(ImageType.SCREENSHOTS);

		if(images != null) {
			return images.split(";;;");
		}

		return null;
	}

	@Nullable
	public String[] getFlags() {
		return flags;
	}

	@Nullable
	public String getImage(@NotNull ImageType type) {
		return switch(type) {
			case BANNER, LARGE_THUMBNAIL, SMALL_THUMBNAIL, SCREENSHOTS -> images.get(type);

			case BIGGEST -> {
				if(images == null) yield null;

				var banner = images.get(ImageType.BANNER);
				if(banner != null) yield banner;

				var largeThumbnail = images.get(ImageType.LARGE_THUMBNAIL);
				if(largeThumbnail != null) yield largeThumbnail;

				yield images.get(ImageType.SMALL_THUMBNAIL);
			}

			case SMALLEST -> {
				if(images == null) yield null;

				var smallThumbnail = images.get(ImageType.SMALL_THUMBNAIL);
				if(smallThumbnail != null) yield smallThumbnail;

				var largeThumbnail = images.get(ImageType.LARGE_THUMBNAIL);
				if(largeThumbnail != null) yield largeThumbnail;

				yield images.get(ImageType.BANNER);
			}

			case LARGE_THUMBNAIL_OR_OTHER -> {
				if(images == null) yield null;

				var largeThumbnail = images.get(ImageType.LARGE_THUMBNAIL);
				if(largeThumbnail != null) yield largeThumbnail;

				var smallThumbnail = images.get(ImageType.SMALL_THUMBNAIL);
				if(smallThumbnail != null) yield smallThumbnail;

				yield images.get(ImageType.BANNER);
			}
		};
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public String toString() {
		if(adapter == null) {
			adapter = new Moshi.Builder().build().adapter(Media.class);
		}

		return adapter.toJson(this);
	}

	public String getId(String idName) {
		var ids = getIds();

		if(ids == null) {
			return null;
		}

		return ids.get(idName);
	}

	public String getManagerId() {
		return globalId.split(";;;")[0];
	}

	public String getExtensionId() {
		return globalId.split(";;;")[1].split(":")[1];
	}

	public String getProviderId() {
		return globalId.split(";;;")[1].split(":")[0];
	}

	public String getLocalId() {
		return globalId.split(";;;")[2];
	}

	@Nullable
	public String[] getGenres() {
		return genres;
	}

	/**
	 * Valid syntax of an item:
	 * <p>My tag</p>
	 * <p>My Spoiler tag:::SPOILER:::</p>
	 */
	@Nullable
	public String[] getTags() {
		return tags;
	}

	@Nullable
	public Float getScore() {
		return score;
	}

	@Nullable
	public ExternalService[] getExternalServices() {
		return externalServices;
	}

	@Nullable
	public Integer getEpisodesCount() {
		return episodes;
	}

	@Nullable
	public Long getReleaseDate() {
		return releaseDate;
	}

	@Nullable
	public Long getDuration() {
		return duration;
	}

	@Nullable
	public String getAgeRating() {
		return ageRating;
	}

	@Nullable
	public String getCountry() {
		return country;
	}

	@Nullable
	public User[] getAuthors() {
		return authors;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	/**
	 * @return Either the MAIN's {@link ExternalService} url or either an url of the first one.
	 * @author MrBoomDev
	 */
	@Nullable
	public String getUrl() {
		var externals = getExternalServices();
		if(externals == null || externals.length == 0) return null;

		for(var external : externals) {
			if(Awery.MAIN.equals(external.getTitle())) {
				return external.getUrl();
			}
		}

		return externals[0].getUrl();
	}

	@Nullable
	public Map<String, String> getIds() {
		return ids;
	}

	@Nullable
	public String getExtra() {
		return extra;
	}

	@Nullable
	public String[] getTitles() {
		return titles;
	}

	@Nullable
	public String getTitle() {
		var titles = getTitles();
		if(titles == null || titles.length == 0) return null;

		return titles[0];
	}

	public static class Builder {
		private final @NotNull Media media;

		public Builder(@NotNull Media media) {
			this.media = media;
		}

		/**
		 * @param globalId The unique id of the media in the following format:
		 * <p>{@code MANAGER_ID;;;PROVIDER_ID:EXTENSION_ID;;;ITEM_ID}</p>
		 */
		public Builder(@NotNull Type type, @NotNull String globalId) {
			this.media = new Media(type, globalId);
		}

		public Builder(
				@NotNull Type type,
				@NotNull String managerId,
				@NotNull String extensionId,
				@NotNull String providerId,
				@NotNull String localId
		) {
			this(type, managerId + ";;;" + providerId + ":" + extensionId + ";;;" + localId);
		}

		public Builder setStatus(Status status) {
			media.status = status;
			return this;
		}

		public Builder setTitles(String... titles) {
			media.titles = titles;
			return this;
		}

		public Builder setRatings(ExternalService... ratings) {
			media.externalServices = ratings;
			return this;
		}

		public Builder setCountry(String country) {
			media.country = country;
			return this;
		}

		public Builder setAuthors(User... authors) {
			media.authors = authors;
			return this;
		}

		public Builder setAgeRating(String ageRating) {
			media.ageRating = ageRating;
			return this;
		}

		public Builder setDescription(String description) {
			media.description = description;
			return this;
		}

		public Builder setDuration(Long duration) {
			media.duration = duration;
			return this;
		}

		public Builder setEpisodesCount(Integer count) {
			media.episodes = count;
			return this;
		}

		public Builder setReleaseDate(Long releaseDate) {
			media.releaseDate = releaseDate;
			return this;
		}

		public Builder setScore(Float score) {
			media.score = score;
			return this;
		}

		public Builder setGenres(String... genres) {
			media.genres = genres;
			return this;
		}

		/**
		 * Valid syntax of an item:
		 * <p>My tag</p>
		 * <p>My Spoiler tag:::SPOILER:::</p>
		 */
		public Builder setTags(String... tags) {
			media.tags = tags;
			return this;
		}

		public Builder setImage(@NotNull ImageType imageType, String theImage) {
			return switch(imageType) {
				case BANNER, LARGE_THUMBNAIL, SMALL_THUMBNAIL, SCREENSHOTS -> {
					if(media.images == null) {
						media.images = new HashMap<>();
						media.images.put(imageType, theImage);
					}

					yield this;
				}

				case SMALLEST -> setImage(ImageType.SMALL_THUMBNAIL, theImage);
				case BIGGEST -> setImage(ImageType.BANNER, theImage);
				case LARGE_THUMBNAIL_OR_OTHER -> setImage(ImageType.LARGE_THUMBNAIL, theImage);
			};
		}

		public Builder setFlags(String... flags) {
			media.flags = flags;
			return this;
		}

		public Builder setScreenshots(@NotNull String... screenshots) {
			return setImage(ImageType.SCREENSHOTS, String.join(";;;", screenshots));
		}

		public Builder setIds(Map<String, String> ids) {
			media.ids = ids;
			return this;
		}

		public Builder setId(String idName, String idValue) {
			if(media.ids == null) {
				media.ids = new HashMap<>();
			}

			media.ids.put(idName, idValue);
			return this;
		}

		public Builder setExtra(String extra) {
			media.extra = extra;
			return this;
		}

		public Builder setExternalServices(ExternalService... services) {
			media.externalServices = services;
			return this;
		}

		public Builder setUrl(String url) {
			if(url == null) return this;

			var main = new ExternalService.Builder()
					.setTitle(Awery.MAIN)
					.setUrl(url)
					.build();

			if(media.externalServices == null) {
				media.externalServices = new ExternalService[] { main };
			} else {
				var newArray = Arrays.copyOf(media.externalServices, media.externalServices.length + 1);
				newArray[newArray.length - 1] = main;
				media.externalServices = newArray;
			}

			return this;
		}

		public Media build() {
			return new Media(media);
		}
	}
}