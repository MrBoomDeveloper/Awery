package com.mrboomdev.awery.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.ui.activity.MediaActivity;

import java.util.concurrent.atomic.AtomicReference;

import ani.awery.databinding.PopupMediaActionsBinding;

public class MediaUtils {

	public static void launchMediaActivity(Context context, @NonNull CatalogMedia media, String action) {
		var intent = new Intent(context, MediaActivity.class);
		intent.putExtra("media", media.toString());
		intent.putExtra("action", action);
		context.startActivity(intent);
	}

	public static void launchMediaActivity(Context context, CatalogMedia media) {
		launchMediaActivity(context, media, "info");
	}

	public static void openMediaActionsMenu(Context context, @NonNull CatalogMedia media) {
		final var dialog = new AtomicReference<Dialog>();
		var inflater = LayoutInflater.from(context);
		var binding = PopupMediaActionsBinding.inflate(inflater);

		binding.title.setText(media.title);

		Glide.with(context)
				.load(media.poster.large)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(binding.poster);

		binding.play.setOnClickListener(v -> {
			launchMediaActivity(context, media);
			dialog.get().dismiss();
		});

		binding.bookmark.setOnClickListener(v -> {
			openMediaBookmarkMenu(context, media);
			dialog.get().dismiss();
		});

		var sheet = new BottomSheetDialog(context);
		dialog.set(sheet);

		sheet.getBehavior().setPeekHeight(1000);
		sheet.setContentView(binding.getRoot());
		sheet.show();
	}

	public static void openMediaBookmarkMenu(Context context, CatalogMedia media) {

	}
}