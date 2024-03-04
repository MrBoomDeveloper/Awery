package com.mrboomdev.awery.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.data.db.DBCatalogMedia;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.util.observable.ObservableArrayList;
import com.mrboomdev.awery.util.observable.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryFragment extends MediaCatalogFragment {
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();
	private final ObservableList<MediaCategoriesAdapter.Category> categories = new ObservableArrayList<>();
	private static LibraryFragment instance;
	private boolean didDataChanged;
	private long loadId;

	@Override
	public void onResume() {
		super.onResume();

		if(didDataChanged) {
			loadData();
			didDataChanged = false;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		instance = null;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		getConcatAdapter().addAdapter(getHeaderAdapter());
		getConcatAdapter().addAdapter(getEmptyAdapter());
		getConcatAdapter().addAdapter(categoriesAdapter);

		categoriesAdapter.setCategories(categories);
		loadData();

		getBinding().swipeRefresher.setOnRefreshListener(() -> {
			loadData();
			getBinding().swipeRefresher.setRefreshing(false);
		});

		instance = this;
	}

	public static void notifyDataChanged() {
		if(instance == null) return;

		if(instance.isVisible()) {
			AweryApp.runOnUiThread(() -> {
				if(instance == null) return;
				instance.loadData();
			});
		} else {
			instance.didDataChanged = true;
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void loadData() {
		var wasLoadId = ++loadId;
		categories.clear(false);
		categoriesAdapter.notifyDataSetChanged();

		setEmptyData(true);
		getEmptyAdapter().setEnabled(true);

		new Thread(() -> {
			var db = AweryApp.getDatabase();
			var lists = db.getListDao().getAll();
			var categories = new ArrayList<MediaCategoriesAdapter.Category>();

			for(var list : lists) {
				var dbMediaList = db.getMediaDao().getAllFromList(list.getId());
				if(dbMediaList.isEmpty()) continue;

				var mediaList = dbMediaList.stream()
						.map(DBCatalogMedia::toCatalogMedia)
						.collect(Collectors.toList());

				var category = new MediaCategoriesAdapter.Category(list.getName(), mediaList);
				categories.add(category);
			}

			loadCategories(categories, wasLoadId);
		}).start();
	}

	@SuppressLint("NotifyDataSetChanged")
	private void loadCategories(List<MediaCategoriesAdapter.Category> categories, long loadId) {
		if(loadId != this.loadId) return;

		AweryApp.runOnUiThread(() -> {
			if(loadId != this.loadId) return;

			if(categories.isEmpty()) {
				setEmptyData(false,
						"Library is empty",
						"Start browsing the catalog and new things will appear here!");

				return;
			}

			this.categories.addAll(categories);
			getEmptyAdapter().setEnabled(false);
			categoriesAdapter.notifyDataSetChanged();

			// We do this to prevent screen from being scrolling a little bit
			AweryApp.runDelayed(() -> {
				if(loadId != this.loadId) return;
				getBinding().catalogCategories.scrollToPosition(0);
			}, 100);
		});
	}
}