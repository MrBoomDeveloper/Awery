package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.anilist.AnilistApi;
import com.mrboomdev.awery.catalog.anilist.query.AnilistQuery;
import com.mrboomdev.awery.catalog.anilist.query.AnilistTagsQuery;
import com.mrboomdev.awery.catalog.anilist.query.AnilistTrendingQuery;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.ui.adapter.MediaPagerAdapter;

public class AnimeFragment extends MediaCatalogFragment {
	private final MediaPagerAdapter pagerAdapter = new MediaPagerAdapter();
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var config = new ConcatAdapter.Config.Builder()
				.setIsolateViewTypes(true)
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		var concatAdapter = new ConcatAdapter(config, pagerAdapter, categoriesAdapter);
		getBinding().catalogCategories.setHasFixedSize(true);
		getBinding().catalogCategories.setAdapter(concatAdapter);

		loadData();
		getBinding().swipeRefresher.setOnRefreshListener(this::loadData);
	}

	private void loadData() {
		AnilistTrendingQuery.getAnime().executeQuery(items -> {
			requireActivity().runOnUiThread(() -> pagerAdapter.setItems(items));
			getBinding().swipeRefresher.setRefreshing(false);
		}).catchExceptions(e -> {
			AweryApp.toast("Failed to load data", 1);
			getBinding().swipeRefresher.setRefreshing(false);
			e.printStackTrace();
		});
	}
}