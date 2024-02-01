package com.mrboomdev.awery.catalog.anilist.data;

import com.mrboomdev.awery.catalog.template.CatalogMedia;

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
	public Integer id, duration, episodes;
	public boolean isAdult;

	public enum MediaFormat {
		TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, ONE_SHOT, NOVEL, MANGA
	}

	public enum MediaType {
		ANIME, MANGA
	}

	public static class MediaTitle {
		public String romaji, english;
	}

	public static class CoverImage {
		public String extraLarge, large, color, medium;
	}

	public CatalogMedia<AnilistMedia> toCatalogMedia() {
		var media = new CatalogMedia<AnilistMedia>();
		media.title = Objects.requireNonNullElse(title.english, title.romaji);
		media.description = description;
		media.banner = bannerImage;
		media.color = coverImage.color;
		media.originalData = this;
		media.genres = new ArrayList<>(genres);

		media.tags = tags.stream()
				.map(AnilistTag::toCatalogTag)
				.collect(Collectors.toList());

		var posterVersions = new CatalogMedia.ImageVersions();
		posterVersions.medium = coverImage.medium;
		posterVersions.large = coverImage.large;
		posterVersions.extraLarge = coverImage.extraLarge;
		media.poster = posterVersions;

		return media;
	}
}