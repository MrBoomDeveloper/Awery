package com.mrboomdev.awery.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.AweryApp;
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

public class AnimeFragment extends MediaCatalogFragment {
	private final MediaPagerAdapter pagerAdapter = new MediaPagerAdapter();
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();
	private int loadId = 0, doneTasks, totalTasks, doneSuccessfully;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		pagerAdapter.getView((_view, didJustCreated) -> {
			var headerBinding = pagerAdapter.getHeader();
			setupHeader(headerBinding);
		});

		var concatAdapter = getConcatAdapter();
		concatAdapter.addAdapter(pagerAdapter);
		concatAdapter.addAdapter(getHeaderAdapter());
		concatAdapter.addAdapter(getEmptyAdapter());
		concatAdapter.addAdapter(categoriesAdapter);

		loadData();
		getBinding().swipeRefresher.setOnRefreshListener(this::loadData);
	}

	@SuppressLint("NotifyDataSetChanged")
	private void loadData() {
		totalTasks = 0;
		doneSuccessfully = 0;
		doneTasks = 0;
		var currentLoadId = ++loadId;

		pagerAdapter.setItems(Collections.emptyList());
		pagerAdapter.setEnabled(false);

		getHeaderAdapter().setEnabled(true);
		getEmptyAdapter().setEnabled(true);
		setEmptyData(true);

		totalTasks++;
		AnilistSeasonQuery.getCurrentAnimeSeason().executeQuery(items -> requireActivity().runOnUiThread(() -> {
			if(currentLoadId != loadId) return;

			pagerAdapter.setItems(items);
			pagerAdapter.setEnabled(true);
			getHeaderAdapter().setEnabled(false);

			finishedLoading(currentLoadId, null);
		})).catchExceptions(e -> requireActivity().runOnUiThread(() -> {
			if(currentLoadId != loadId) return;
			finishedLoading(currentLoadId, e);
		}));

		var cats = new ObservableArrayList<MediaCategoriesAdapter.Category>();
		categoriesAdapter.setCategories(cats);

		loadCategory("Trending", currentLoadId, AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.TRENDING_DESC,
				null, null, false
		), cats);

		{ //TODO: REMOVE THIS SHIT BEFORE RELEASE
			var file = new File(requireContext().getExternalFilesDir(null), "hentai.txt");

			if(file.exists()) {
				loadCategory("Hentai", currentLoadId, AnilistSearchQuery.search(
						AnilistMedia.MediaType.ANIME,
						AnilistQuery.MediaSort.TRENDING_DESC,
						null, null, true
				), cats);
			}
		}

		loadCategory("Recent Updates", currentLoadId, AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.UPDATED_AT_DESC,
				null, null, false
		), cats);

		loadCategory("Popular", currentLoadId, AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.POPULARITY_DESC,
				null, null, false
		), cats);

		loadCategory("Movies", currentLoadId, AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.TRENDING_DESC,
				AnilistMedia.MediaFormat.MOVIE, null, false
		), cats);

		loadCategory("Most Favorite", currentLoadId, AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.FAVOURITES_DESC,
				null, null, false
		), cats);

		loadCategory("The Best Anime", currentLoadId, AnilistSearchQuery.search(
				AnilistMedia.MediaType.ANIME,
				AnilistQuery.MediaSort.SCORE_DESC,
				null, null, false
		), cats);
	}

	private void finishedLoading(int loadId, Throwable t) {
		if(loadId != this.loadId) return;
		doneTasks++;

		if(t != null) {
			t.printStackTrace();
		} else {
			if(doneSuccessfully++ == 0) {
				// We do this to prevent screen from being scrolling a little bit
				AweryApp.runDelayed(() -> requireActivity().runOnUiThread(() ->
						getBinding().catalogCategories.scrollToPosition(0)), 100);
			}

			requireActivity().runOnUiThread(() -> {
				getBinding().swipeRefresher.setRefreshing(false);
				getEmptyAdapter().setEnabled(false);
			});
		}

		if(doneTasks == totalTasks && doneSuccessfully == 0) {
			requireActivity().runOnUiThread(() -> {
				getBinding().swipeRefresher.setRefreshing(false);
				getEmptyAdapter().setEnabledSuperForce(true);

				setEmptyData(false, "Nothing found!",
						"It looks like there's nothing here. Try installing some extensions to see something different!");
			});
		}
	}

	private void loadCategory(
			String title,
			int loadId,
			@NonNull AnilistQuery<Collection<CatalogMedia>> query,
			ObservableList<MediaCategoriesAdapter.Category> list
	) {
		totalTasks++;

		query.executeQuery(items -> requireActivity().runOnUiThread(() -> {
			if(loadId != this.loadId) return;
			list.add(new MediaCategoriesAdapter.Category(title, items));
			finishedLoading(loadId, null);
		})).catchExceptions(t -> finishedLoading(loadId, t));
	}
}