package com.mrboomdev.awery.catalog.template;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.squareup.moshi.Json;
import com.squareup.moshi.Moshi;

import java.util.ArrayList;
import java.util.List;

import ani.awery.databinding.ActivityNoInternetBinding;

public class CatalogMedia {
	public static final CatalogMedia INVALID_MEDIA;
	public List<String> titles = new ArrayList<>();
	public String title, originalTitle, banner, description, color, url;
	public MediaType type;
	public ImageVersions poster;
	public int latestEpisode, id;
	public Float averageScore;
	public List<CatalogTag> tags;
	public List<String> genres;
	public MediaStatus status;
	@Json(ignore = true)
	public Drawable cachedBanner;

	public void handleClick(Context context, String action) {
		var intent = new Intent(context, MediaActivity.class);
		intent.putExtra("media", this.toString());
		intent.putExtra("action", action);
		context.startActivity(intent);
	}

	public void handleClick(Context context) {
		handleClick(context, "info");
	}

	@NonNull
	@Override
	public String toString() {
		var moshi = new Moshi.Builder().build();
		var adapter = moshi.adapter(CatalogMedia.class);
		return adapter.toJson(this);
	}

	public void setTitle(String title) {
		this.title = title;
		this.titles = List.of(title);
	}

	public String getBestBanner() {
		if(banner != null) return banner;
		return getBestPoster();
	}

	public String getBestPoster() {
		if(poster.extraLarge != null) return poster.extraLarge;
		if(poster.large != null) return poster.large;
		if(poster.medium != null) return poster.medium;
		return banner;
	}

	public void setPoster(String poster) {
		this.poster = new ImageVersions();
		this.poster.extraLarge = poster;
		this.poster.large = poster;
		this.poster.medium = poster;
	}

	public void handleLongClick(Context context) {
		var inflater = LayoutInflater.from(context);
		var binding = ActivityNoInternetBinding.inflate(inflater);

		var sheet = new BottomSheetDialog(context);
		sheet.setContentView(binding.getRoot());
		sheet.show();
	}

	public enum MediaStatus {
		ONGOING, COMPLETED, COMING_SOON, PAUSED, CANCELLED
	}

	public enum MediaType {
		MOVIE, BOOK, TV, POST
	}

	public static class ImageVersions {
		public String extraLarge, large, medium;
	}

	static {
		INVALID_MEDIA = new CatalogMedia();
		INVALID_MEDIA.setTitle("Invalid!");
		INVALID_MEDIA.description = "Invalid media item!";
	}
}