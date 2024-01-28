package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;

import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.ui.adapter.MediaPagerAdapter;

public class MangaFragment extends MediaCatalogFragment {

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var pagerAdapter = new MediaPagerAdapter();
		var categoriesAdapter = new MediaCategoriesAdapter();

		for(int i = 0; i < 25; i++) {
			var categoryRecent = new MediaCategoriesAdapter.Category("Recently updated " + i);

			for(int a = 0; a < 25; a++) {
				categoryRecent.items.add("(M) Attack on Titan " + i + " " + a, false);
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
	}
}