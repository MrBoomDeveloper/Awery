package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
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
import com.mrboomdev.awery.catalog.template.CatalogMedia;

import ani.awery.databinding.MediaDetailsOverviewLayoutBinding;

public class MediaInfoFragment extends Fragment {
	private final ConcatAdapter concatAdapter;
	private MediaDetailsOverviewLayoutBinding binding;
	private CatalogMedia pendingMedia;

	public MediaInfoFragment(CatalogMedia media) {
		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		concatAdapter = new ConcatAdapter(config);
		setData(media);
	}

	public MediaInfoFragment() {
		this(null);
	}

	public void setData(CatalogMedia media) {
		if(media == null) return;

		if(binding == null) {
			pendingMedia = media;
			return;
		}

		Glide.with(binding.getRoot())
				.load(media.poster.extraLarge)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(binding.poster);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = MediaDetailsOverviewLayoutBinding.inflate(inflater, container, false);

		binding.items.setLayoutManager(new LinearLayoutManager(
				inflater.getContext(),
				LinearLayoutManager.VERTICAL,
				false));

		binding.items.setAdapter(concatAdapter);

		setData(pendingMedia);
		return binding.getRoot();
	}
}