package com.mrboomdev.awery.catalog.template;

import android.graphics.drawable.Drawable;

import com.squareup.moshi.Json;

import java.util.List;

public class CatalogMedia<T> {
	public T originalData;
	public String title, banner, description, color;
	public MediaType type;
	public ImageVersions poster;
	public int latestEpisode, averageScore, id;
	public List<CatalogTag> tags;
	public List<String> genres;
	@Json(ignore = true)
	public Drawable cachedBanner;

	public enum MediaType {
		MOVIE, BOOK, TV
	}

	public static class ImageVersions {
		public String extraLarge, large, medium;
	}
}