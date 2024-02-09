package com.mrboomdev.awery.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;
import com.mrboomdev.awery.catalog.anilist.query.AnilistSeasonQuery;
import com.mrboomdev.awery.catalog.anilist.query.AnilistTrendingQuery;
import com.mrboomdev.awery.ui.activity.SettingsActivity;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.ui.adapter.MediaPagerAdapter;
import com.mrboomdev.awery.util.ObservableArrayList;

import java.util.Collections;
import java.util.List;

import ani.awery.databinding.HeaderLayoutBinding;
import ani.awery.media.SearchActivity;

public class AnimeFragment extends MediaCatalogFragment {
	private final MediaPagerAdapter pagerAdapter = new MediaPagerAdapter();
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var header = HeaderLayoutBinding.inflate(getLayoutInflater());
		pagerAdapter.setHeaderView(header.getRoot());
		setupHeader(header);

		var concatAdapter = getConcatAdapter();
		concatAdapter.addAdapter(pagerAdapter);
		concatAdapter.addAdapter(categoriesAdapter);

		loadData();
		getBinding().swipeRefresher.setOnRefreshListener(this::loadData);
	}

	@SuppressLint("NotifyDataSetChanged")
	private void loadData() {
		pagerAdapter.setItems(Collections.emptyList());
		pagerAdapter.setEnabled(true);
		pagerAdapter.setIsLoading(true);

		getConcatAdapter().removeAdapter(getHeaderAdapter());

		AnilistSeasonQuery.getCurrentAnimeSeason().executeQuery(items -> requireActivity().runOnUiThread(() -> {
			pagerAdapter.setItems(items);
			pagerAdapter.setIsLoading(false);
		})).catchExceptions(e -> requireActivity().runOnUiThread(() -> {
			pagerAdapter.setEnabled(false);
			e.printStackTrace();

			getConcatAdapter().addAdapter(0, getHeaderAdapter());
		})).onFinally(() -> getBinding().swipeRefresher.setRefreshing(false));

		var cats = new ObservableArrayList<MediaCategoriesAdapter.Category>();
		categoriesAdapter.setCategories(cats);

		AnilistTrendingQuery.getAnime().executeQuery(items -> requireActivity().runOnUiThread(() -> {
			cats.add(new MediaCategoriesAdapter.Category("Trending", items));
		})).catchExceptions(e -> {
			e.printStackTrace();
		}).onFinally(() -> getBinding().swipeRefresher.setRefreshing(false));

		AnilistTrendingQuery.getAnime().executeQuery(items -> requireActivity().runOnUiThread(() -> {
			cats.add(new MediaCategoriesAdapter.Category("Recommended", items));
		})).catchExceptions(e -> {
			e.printStackTrace();
		}).onFinally(() -> getBinding().swipeRefresher.setRefreshing(false));

		AnilistTrendingQuery.getAnime().executeQuery(items -> requireActivity().runOnUiThread(() -> {
			cats.add(new MediaCategoriesAdapter.Category("Popular", items));
		})).catchExceptions(e -> {
			e.printStackTrace();
		}).onFinally(() -> getBinding().swipeRefresher.setRefreshing(false));
	}
}