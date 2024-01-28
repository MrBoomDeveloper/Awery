package com.mrboomdev.awery.catalog.anilist.data;

import com.mrboomdev.awery.catalog.template.CatalogMedia;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AnilistTrendingMedia {
	public AnilistMedia media;
	public int averageScore;

	public CatalogMedia<AnilistTrendingMedia> toCatalogMedia() {
		var media = new CatalogMedia<AnilistTrendingMedia>();
		media.title = this.media.title.english;
		media.description = this.media.description;
		media.banner = this.media.bannerImage;
		media.color = this.media.coverImage.color;
		media.originalData = this;
		media.genres = new ArrayList<>(this.media.genres);

		media.tags = this.media.tags.stream()
				.map(AnilistTag::toCatalogTag)
				.collect(Collectors.toList());

		var posterVersions = new CatalogMedia.ImageVersions();
		posterVersions.medium = this.media.coverImage.medium;
		posterVersions.large = this.media.coverImage.large;
		posterVersions.extraLarge = this.media.coverImage.extraLarge;
		media.poster = posterVersions;

		return media;
	}
}