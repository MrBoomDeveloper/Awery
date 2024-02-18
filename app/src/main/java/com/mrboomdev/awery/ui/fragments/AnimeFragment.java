package com.mrboomdev.awery.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.catalog.anilist.data.AnilistMedia;
import com.mrboomdev.awery.catalog.anilist.query.AnilistQuery;
import com.mrboomdev.awery.catalog.anilist.query.AnilistSearchQuery;
import com.mrboomdev.awery.catalog.anilist.query.AnilistSeasonQuery;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.ui.adapter.MediaPagerAdapter;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import ani.awery.databinding.LayoutHeaderBinding;

public class AnimeFragment extends MediaCatalogFragment {
	private final MediaPagerAdapter pagerAdapter = new MediaPagerAdapter();
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var header = LayoutHeaderBinding.inflate(getLayoutInflater());
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

		loadCategory("Trending", AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.TRENDING_DESC,
				null, null, false
		), cats);

		{ //TODO: REMOVE THIS SHIT BEFORE RELEASE
			var file = new File(requireContext().getExternalFilesDir(null), "hentai.txt");

			if(file.exists()) {
				loadCategory("Hentai", AnilistSearchQuery.search(
						AnilistMedia.MediaType.ANIME,
						AnilistQuery.MediaSort.TRENDING_DESC,
						null, null, true
				), cats);
			}
		}

		loadCategory("Recent Updates", AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.UPDATED_AT_DESC,
				null, null, false
		), cats);

		loadCategory("Popular", AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.POPULARITY_DESC,
				null, null, false
		), cats);

		loadCategory("Movies", AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.TRENDING_DESC,
				AnilistMedia.MediaFormat.MOVIE, null, false
		), cats);

		loadCategory("Most Favorite", AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.FAVOURITES_DESC,
				null, null, false
		), cats);

		loadCategory("The Best Anime", AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.SCORE_DESC,
				null, null, false
		), cats);
	}

	private void loadCategory(String title, @NonNull AnilistQuery<Collection<CatalogMedia>> query, ObservableList<MediaCategoriesAdapter.Category> list) {
		query.executeQuery(items -> requireActivity().runOnUiThread(() ->
				list.add(new MediaCategoriesAdapter.Category(title, items))))
		.catchExceptions(Throwable::printStackTrace).onFinally(() ->
				getBinding().swipeRefresher.setRefreshing(false));
	}
}