package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;

import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.ui.adapter.MediaPagerAdapter;

public class MangaFragment extends MediaCatalogListsFragment {

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var pagerAdapter = new MediaPagerAdapter();
		var categoriesAdapter = new MediaCategoriesAdapter();

		var config = new ConcatAdapter.Config.Builder()
				.setIsolateViewTypes(true)
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		var concatAdapter = new ConcatAdapter(config, pagerAdapter, categoriesAdapter);
		getBinding().catalogCategories.setHasFixedSize(true);
		getBinding().catalogCategories.setAdapter(concatAdapter);
	}
}