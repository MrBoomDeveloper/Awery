package com.mrboomdev.awery.extensions.support.anilist.data;

import static com.mrboomdev.awery.data.Constants.ANILIST_CATALOG_ITEM_ID_PREFIX;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AnilistMedia {
	public List<AnilistEpisode> streamingEpisodes;
	public List<String> genres;
	public List<AnilistTag> tags;
	public String description, bannerImage, countryOfOrigin;
	public CoverImage coverImage;
	public MediaTitle title;
	public MediaFormat format;
	public Integer id, duration, episodes, averageScore;
	public MediaDate startDate;
	public boolean isAdult;
	public MediaStatus status;

	public enum MediaFormat {
		TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, ONE_SHOT, NOVEL, MANGA
	}

	public static class MediaDate {
		public Integer year, month, day;
	}

	public enum MediaStatus {
		FINISHED, RELEASING, NOT_YET_RELEASED, CANCELLED, HIATUS
	}

	public enum MediaType {
		ANIME, MANGA
	}

	public static class MediaTitle {
		@Json(name = "native")
		public String nativeTitle;
		public String romaji, english;
	}

	public static class CoverImage {
		public String extraLarge, large, color, medium;
	}

	public CatalogMedia toCatalogMedia() {
		var media = new CatalogMedia(ANILIST_CATALOG_ITEM_ID_PREFIX + id);
		media.description = description;
		media.setId("anilist", String.valueOf(id));
		media.banner = bannerImage;
		media.duration = duration;
		media.episodesCount = episodes;
		media.country = countryOfOrigin;
		media.averageScore = (averageScore != null) ? (averageScore / 10f) : null;
		media.genres = new ArrayList<>(genres);
		media.ageRating = isAdult ? "R" : null;

		if(startDate != null && startDate.year != null) {
			media.releaseDate = Calendar.getInstance();

			if(startDate.year != null) media.releaseDate.set(Calendar.YEAR, startDate.year);
			if(startDate.month != null) media.releaseDate.set(Calendar.MONTH, startDate.month - 1);
			if(startDate.day != null) media.releaseDate.set(Calendar.DAY_OF_MONTH, startDate.day);
		}

		if(title.english != null) media.titles.add(title.english);
		if(title.romaji != null) media.titles.add(title.romaji);
		if(title.nativeTitle != null) media.titles.add(title.nativeTitle);

		media.tags = stream(tags)
				.map(AnilistTag::toCatalogTag)
				.toList();

		media.status = switch(status) {
			case RELEASING -> CatalogMedia.MediaStatus.ONGOING;
			case FINISHED -> CatalogMedia.MediaStatus.COMPLETED;
			case HIATUS -> CatalogMedia.MediaStatus.PAUSED;
			case CANCELLED -> CatalogMedia.MediaStatus.CANCELLED;
			default -> CatalogMedia.MediaStatus.COMING_SOON;
		};

		media.poster.medium = coverImage.medium;
		media.poster.large = coverImage.large;
		media.poster.extraLarge = coverImage.extraLarge;

		return media;
	}
}