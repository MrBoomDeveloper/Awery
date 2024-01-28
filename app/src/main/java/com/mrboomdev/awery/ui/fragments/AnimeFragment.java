package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.anilist.AnilistApi;
import com.mrboomdev.awery.catalog.anilist.query.AnilistTagsQuery;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.ui.adapter.MediaPagerAdapter;

public class AnimeFragment extends MediaCatalogFragment {

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var pagerAdapter = new MediaPagerAdapter();
		var categoriesAdapter = new MediaCategoriesAdapter();

		for(int i = 0; i < 25; i++) {
			var categoryRecent = new MediaCategoriesAdapter.Category("Recently updated " + i);

			for(int a = 0; a < 25; a++) {
				categoryRecent.items.add("(A) Attack on Titan " + i + " " + a, false);
			}

			categoriesAdapter.addCategory(categoryRecent, false);
		}

		pagerAdapter.addItem("sussy baka", false);
		pagerAdapter.addItem("sussy baka 2", false);

		var config = new ConcatAdapter.Config.Builder()
				.setIsolateViewTypes(true)
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		var concatAdapter = new ConcatAdapter(config, pagerAdapter, categoriesAdapter);
		getBinding().catalogCategories.setHasFixedSize(true);
		getBinding().catalogCategories.setAdapter(concatAdapter);

		AnilistTagsQuery.getTags(AnilistTagsQuery.ALL).executeQuery(response -> {
			AweryApp.toast("loaded! ", 1);
			System.out.println(response);
		});
	}
}