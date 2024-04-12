package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.support.anilist.data.AnilistMedia;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistQuery;
import com.mrboomdev.awery.extensions.support.anilist.query.AnilistSearchQuery;
import com.mrboomdev.awery.ui.adapter.FeaturedMediaAdapter;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AnimeFragment extends MediaCatalogListsFragment {
	private static final String TAG = "AnimeFragment";
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();
	private final FeaturedMediaAdapter pagerAdapter = new FeaturedMediaAdapter();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private int loadId = 0, doneTasks, totalTasks, doneSuccessfully;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		pagerAdapter.getView((_view) -> {
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
		AnilistSearchQuery.builder()
				.setCurrentSeason()
				.setIsAdult(AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT) ? null : false)
				.setType(AnilistMedia.MediaType.ANIME)
				.setSort(AnilistQuery.MediaSort.POPULARITY_DESC)
				.build().executeQuery(requireContext(), items -> {
					if(currentLoadId != loadId) return;

					var filtered = new ArrayList<>(MediaUtils.filterMediaSync(items));
					if(filtered.isEmpty()) throw new ZeroResultsException("No media was found", R.string.no_media_found);

					requireActivity().runOnUiThread(() -> {
						if(currentLoadId != loadId) return;

						pagerAdapter.setItems(filtered);
						pagerAdapter.setEnabled(true);
						getHeaderAdapter().setEnabled(false);

						finishedLoading(currentLoadId, null);
					});
				}).catchExceptions(e -> requireActivity().runOnUiThread(() -> {
					if(currentLoadId != loadId) return;
					finishedLoading(currentLoadId, e);
				}));

		var cats = new ArrayList<MediaCategoriesAdapter.Category>();
		categoriesAdapter.setCategories(cats);

		loadCategory("Trending", currentLoadId, AnilistSearchQuery.builder()
				.setType(AnilistMedia.MediaType.ANIME)
				.setSort(AnilistQuery.MediaSort.TRENDING_DESC)
				.setIsAdult(AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT) ? null : false)
				.build(), cats);

		{ //TODO: REMOVE THIS SHIT BEFORE RELEASE
			var file = new File(requireContext().getExternalFilesDir(null), "hentai.txt");

			if(file.exists()) {
				loadCategory("Hentai", currentLoadId, AnilistSearchQuery.builder()
						.setType(AnilistMedia.MediaType.ANIME)
						.setSort(AnilistQuery.MediaSort.TRENDING_DESC)
						.setIsAdult(true)
						.build(), cats);
			}
		}

		loadCategory("Popular", currentLoadId, AnilistSearchQuery.builder()
				.setType(AnilistMedia.MediaType.ANIME)
				.setSort(AnilistQuery.MediaSort.POPULARITY_DESC)
				.setIsAdult(AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT) ? null : false)
				.build(), cats);

		loadCategory("Movies", currentLoadId, AnilistSearchQuery.builder()
				.setType(AnilistMedia.MediaType.ANIME)
				.setSort(AnilistQuery.MediaSort.TRENDING_DESC)
				.setFormat(AnilistMedia.MediaFormat.MOVIE)
				.setIsAdult(AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT) ? null : false)
				.build(), cats);

		loadCategory("Most Favorited", currentLoadId, AnilistSearchQuery.builder()
				.setType(AnilistMedia.MediaType.ANIME)
				.setSort(AnilistQuery.MediaSort.FAVOURITES_DESC)
				.setIsAdult(AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT) ? null : false)
				.build(), cats);

		loadCategory("Top Rated", currentLoadId, AnilistSearchQuery.builder()
				.setType(AnilistMedia.MediaType.ANIME)
				.setSort(AnilistQuery.MediaSort.SCORE_DESC)
				.setIsAdult(AwerySettings.getInstance().getBoolean(AwerySettings.content.ADULT_CONTENT) ? null : false)
				.build(), cats);
	}

	private void finishedLoading(int loadId, Throwable t) {
		if(loadId != this.loadId) return;
		doneTasks++;

		if(t != null) {
			Log.e(TAG, "Failed to load", t);
		} else {
			if(doneSuccessfully++ == 0) {
				// We do this to prevent screen from being scrolling a little bit
				runDelayed(() -> {
					if(loadId != this.loadId) return;
					getBinding().catalogCategories.scrollToPosition(0);
				}, 100);
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

				if(t == null) {
					setEmptyData(false,
							"Nothing found!",
							"It looks like there's nothing here. Try installing some extensions to see something different!");
				} else {
					var details = new ExceptionDescriptor(t);

					setEmptyData(false,
							details.getTitle(requireContext()),
							details.getMessage(requireContext()));
				}
			});
		}
	}

	private void loadCategory(
			String title,
			int loadId,
			@NonNull AnilistQuery<Collection<CatalogMedia>> query,
			List<MediaCategoriesAdapter.Category> list
	) {
		totalTasks++;

		query.executeQuery(requireContext(), items -> {
			if(loadId != this.loadId) return;

			var filtered = new ArrayList<>(MediaUtils.filterMediaSync(items));
			if(filtered.isEmpty()) throw new ZeroResultsException("No media was found", R.string.no_media_found);

			requireActivity().runOnUiThread(() -> {
				if(loadId != this.loadId) return;

				var category = new MediaCategoriesAdapter.Category(title, filtered);
				category.id = idGenerator.getLong();

				var wasSize = list.size();
				list.add(category);
				categoriesAdapter.notifyItemInserted(wasSize);

				finishedLoading(loadId, null);
			});
		}).catchExceptions(t -> finishedLoading(loadId, t));
	}
}