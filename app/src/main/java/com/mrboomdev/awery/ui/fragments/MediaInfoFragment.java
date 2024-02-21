package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import ani.awery.databinding.LayoutMediaDetailsBinding;
import ani.awery.databinding.MediaDetailsOverviewLayoutBinding;

public class MediaInfoFragment extends Fragment {
	private final ConcatAdapter concatAdapter;
	private MediaDetailsOverviewLayoutBinding binding;
	private CatalogMedia pendingMedia, media;

	public MediaInfoFragment(CatalogMedia media) {
		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		concatAdapter = new ConcatAdapter(config);
		setMedia(media);
	}

	public MediaInfoFragment() {
		this(null);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("media", media.toString());
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			var moshi = new Moshi.Builder().build();
			var adapter = moshi.adapter(CatalogMedia.class);

			try {
				var mediaJson = savedInstanceState.getString("media");
				if(mediaJson == null) return;

				var media = adapter.fromJson(mediaJson);

				if(media != null) {
					setMedia(media);
				} else {
					throw new IOException("Failed to restore media!");
				}
			} catch(IOException e) {
				e.printStackTrace();
				AweryApp.toast("Failed to restore media!", 1);
			}
		}
	}

	public void setMedia(CatalogMedia media) {
		if(media == null) return;
		this.media = media;

		if(binding == null) {
			pendingMedia = media;
			return;
		}

		Glide.with(binding.getRoot())
				.load(media.poster.extraLarge)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(binding.poster);

		if(media.banner != null) {
			Glide.with(binding.getRoot())
					.load(media.banner)
					.transition(DrawableTransitionOptions.withCrossFade())
					.into(binding.banner);
		}

		concatAdapter.addAdapter(SingleViewAdapter.fromBindingDynamic(parent -> {
			var binding = LayoutMediaDetailsBinding.inflate(getLayoutInflater(), parent, false);
			binding.title.setText(media.title);
			binding.description.setText(Html.fromHtml(media.description, Html.FROM_HTML_MODE_COMPACT).toString().trim());

			for(var tag : media.tags) {
				var chip = new Chip(requireContext());
				chip.setText(tag.name);
				binding.tags.addView(chip);
			}

			return binding;
		}));
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = MediaDetailsOverviewLayoutBinding.inflate(inflater, container, false);

		ViewUtil.setOnApplyUiInsetsListener(binding.posterWrapper, insets -> {
			var margin = ViewUtil.dpPx(8);
			ViewUtil.setTopMargin(binding.posterWrapper, margin + insets.top);
		});

		ViewUtil.setOnApplyUiInsetsListener(binding.items, insets -> {
			var margin = ViewUtil.dpPx(8);

			ViewUtil.setTopPadding(binding.items, insets.top + margin);
			ViewUtil.setBottomPadding(binding.items, insets.bottom + margin);
			ViewUtil.setRightPadding(binding.items, insets.right + (margin * 2));
		});

		binding.items.setLayoutManager(new LinearLayoutManager(
				inflater.getContext(),
				LinearLayoutManager.VERTICAL,
				false));

		binding.items.setAdapter(concatAdapter);

		setMedia(pendingMedia);
		return binding.getRoot();
	}
}