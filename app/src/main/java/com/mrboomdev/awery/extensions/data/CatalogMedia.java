package com.mrboomdev.awery.extensions.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mrboomdev.awery.util.Parser;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogMedia implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	@NonNull
	public String globalId;
	@Nullable
	public List<String> titles;
	@NonNull
	public Map<String, String> ids = new HashMap<>();
	@Nullable
	public Map<String, String> authors;
	@Nullable
	public String banner, description, country, ageRating, extra, url;
	@Nullable
	public MediaType type;
	@Nullable
	public ImageVersions poster = new ImageVersions();
	@Nullable
	public Calendar releaseDate;
	@Nullable
	public Integer duration, episodesCount, latestEpisode;
	@Nullable
	public Float averageScore;
	@Nullable
	public List<CatalogTag> tags;
	@Nullable
	public List<String> genres;
	@Nullable
	public MediaStatus status;

	/**
	 * @param globalId The unique id of the media in the following format:
	 * <p>{@code MANAGER_ID;;;PROVIDER_ID:EXTENSION_ID;;;ITEM_ID}</p>
	 */
	public CatalogMedia(@NonNull String globalId) {
		this.globalId = globalId;
	}

	public CatalogMedia(String managerId, String extensionId, String providerId, String mediaId) {
		this(managerId + ";;;" + providerId + ":" + extensionId + ";;;" + mediaId);
	}

	public CatalogMedia(@NonNull CatalogMedia original) {
		ageRating = original.ageRating;
		banner = original.banner;
		averageScore = original.averageScore;
		country = original.country;
		description = original.description;
		duration = original.duration;
		episodesCount = original.episodesCount;
		extra = original.extra;
		globalId = original.globalId;
		latestEpisode = original.latestEpisode;
		status = original.status;
		type = original.type;
		url = original.url;

		ids = new HashMap<>(original.ids);

		if(original.releaseDate != null) {
			releaseDate = Calendar.getInstance();
			releaseDate.setTimeInMillis(original.releaseDate.getTimeInMillis());
		}

		if(original.titles != null) {
			titles = List.copyOf(original.titles);
		}

		if(original.tags != null) {
			tags = List.copyOf(original.tags);
		}

		if(original.genres != null) {
			genres = List.copyOf(original.genres);
		}

		if(original.authors != null) {
			authors = Map.copyOf(original.authors);
		}

		if(original.poster != null) {
			poster = new ImageVersions(original.poster);
		}
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

	public String getId() {
		return globalId.split(";;;")[2];
	}

	@NonNull
	@Override
	public String toString() {
		return Parser.toString(CatalogMedia.class, this);
	}

	public void setTitle(@NonNull String... titles) {
		this.titles = Lists.newArrayList(titles);
	}

	@Nullable
	public String getTitle() {
		if(titles == null || titles.isEmpty()) return null;
		return titles.get(0);
	}

	public void setTitles(Collection<String> titles) {
		if(titles == null) {
			this.titles = new ArrayList<>();
			return;
		}

		this.titles = List.copyOf(titles);
	}

	@Nullable
	public String getBestBanner() {
		if(banner != null) return banner;
		return getBestPoster();
	}

	public void setId(String type, String id) {
		ids.put(type, id);
	}

	@Nullable
	public String getId(String type) {
		return ids.get(type);
	}

	public String getLargePoster() {
		if(poster != null && poster.large != null) {
			return poster.large;
		}

		return getBestPoster();
	}

	@Nullable
	public String getBestPoster() {
		if(poster == null) {
			return banner;
		}

		if(poster.extraLarge != null) return poster.extraLarge;
		if(poster.large != null) return poster.large;
		if(poster.medium != null) return poster.medium;
		return banner;
	}

	public void setPoster(String poster) {
		if(this.poster == null) {
			this.poster = new ImageVersions();
		}

		this.poster.extraLarge = poster;
		this.poster.large = poster;
		this.poster.medium = poster;
	}

	public enum MediaStatus {
		ONGOING, COMPLETED, COMING_SOON, PAUSED, CANCELLED, UNKNOWN
	}

	public enum MediaType {
		MOVIE, BOOK, TV, POST
	}

	public static class ImageVersions implements Serializable {
		@Serial
		private static final long serialVersionUID = 1;
		public String extraLarge, large, medium;

		public ImageVersions(@NonNull ImageVersions original) {
			extraLarge = original.extraLarge;
			large = original.large;
			medium = original.medium;
		}

		public ImageVersions() {}
	}
}