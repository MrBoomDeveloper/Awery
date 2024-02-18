package com.mrboomdev.awery.catalog.template;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.anilist.data.AnilistMedia;
import com.mrboomdev.awery.ui.activity.MediaDetailsActivity;
import com.squareup.moshi.Json;
import com.squareup.moshi.Moshi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ani.awery.databinding.ActivityNoInternetBinding;
import ani.awery.media.Media;
import ani.awery.media.anime.Anime;

public class CatalogMedia {
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

	public void handleClick(Context context) {
		var moshi = new Moshi.Builder().build();
		var adapter = moshi.adapter(CatalogMedia.class);

		var intent = new Intent(context, MediaDetailsActivity.class);
		intent.putExtra("media", adapter.toJson(this));
		context.startActivity(intent);
	}

	public void setTitle(String title) {
		this.title = title;
		this.titles = List.of(title);
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
}