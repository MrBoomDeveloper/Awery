package com.mrboomdev.awery.catalog.template;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.anilist.data.AnilistMedia;
import com.mrboomdev.awery.ui.activity.MediaDetailsActivity;
import com.squareup.moshi.Json;

import java.util.List;
import java.util.Objects;

import ani.awery.databinding.ActivityNoInternetBinding;
import ani.awery.media.Media;
import ani.awery.media.anime.Anime;

public class CatalogMedia<T> {
	public T originalData;
	public String title, originalTitle, banner, description, color;
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
		var intent = new Intent(context, MediaDetailsActivity.class);
		context.startActivity(intent);
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