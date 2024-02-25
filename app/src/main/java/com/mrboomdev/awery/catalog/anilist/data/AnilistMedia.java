package com.mrboomdev.awery.catalog.anilist.data;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnilistMedia {
	public  List<String> genres;
	public List<AnilistTag> tags;
	public String description, bannerImage;
	public CoverImage coverImage;
	public MediaTitle title;
	public MediaType type;
	public MediaFormat format;
	public Integer id, duration, episodes, averageScore;
	public boolean isAdult;
	public MediaStatus status;

	public enum MediaFormat {
		TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, ONE_SHOT, NOVEL, MANGA
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
		var media = new CatalogMedia(AweryApp.ANILIST_CATALOG_ITEM_ID_PREFIX + id);
		media.title = Objects.requireNonNullElse(title.english, title.romaji);
		media.description = description;
		media.banner = bannerImage;
		media.color = coverImage.color;
		media.globalId = "";
		media.averageScore = (averageScore != null) ? (averageScore / 10f) : null;
		media.genres = new ArrayList<>(genres);

		if(title.english != null) media.titles.add(title.english);
		if(title.romaji != null) media.titles.add(title.romaji);
		if(title.nativeTitle != null) media.titles.add(title.nativeTitle);

		media.tags = tags.stream()
				.map(AnilistTag::toCatalogTag)
				.collect(Collectors.toList());

		media.status = switch(status) {
			case RELEASING -> CatalogMedia.MediaStatus.ONGOING;
			case FINISHED -> CatalogMedia.MediaStatus.COMPLETED;
			case HIATUS -> CatalogMedia.MediaStatus.PAUSED;
			case CANCELLED -> CatalogMedia.MediaStatus.CANCELLED;
			default -> CatalogMedia.MediaStatus.COMING_SOON;
		};

		var posterVersions = new CatalogMedia.ImageVersions();
		posterVersions.medium = coverImage.medium;
		posterVersions.large = coverImage.large;
		posterVersions.extraLarge = coverImage.extraLarge;
		media.poster = posterVersions;

		return media;
	}
}