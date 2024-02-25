package com.mrboomdev.awery.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;

import java.util.Set;
import java.util.stream.Collectors;

public class LibraryFragment extends MediaCatalogFragment {
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();
	private final ObservableList<MediaCategoriesAdapter.Category> categories = new ObservableArrayList<>();

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
	}

	@SuppressLint("NotifyDataSetChanged")
	private void loadData() {
		categories.clear(false);
		categoriesAdapter.notifyDataSetChanged();

		setEmptyData(true);
		getEmptyAdapter().setEnabled(true);

		new Thread(() -> {
			var db = AweryApp.getDatabase();
			var mediaDao = db.getMediaDao();
			var mediaList = mediaDao.getAll();

			for(var a : mediaList) {
				System.out.println(a.title);
			}
		}).start();

		//TODO: Remove old load method
		var saved = AwerySettings.getInstance(AwerySettings.APP_LIBRARY);
		loadCategory("Continue Watching", saved.getStringSet("current"));
		loadCategory("Planned", saved.getStringSet("planned"));
		loadCategory("Delayed", saved.getStringSet("delayed"));
		loadCategory("History", saved.getStringSet("history"));
		loadCategory("Favourite", saved.getStringSet("favourite"));
		loadCategory("Done", saved.getStringSet("done"));
		loadCategory("Dropped", saved.getStringSet("dropped"));

		if(categories.isEmpty()) {
			setEmptyData(false,
					"Library is empty",
					"Start browsing the catalog and new things will appear here!");
		}
	}

	private void loadCategory(String name, @NonNull Set<String> items) {
		if(items.isEmpty()) return;

		var itemsList = items.stream().map(item -> {
			var media = new CatalogMedia("dhdhdhdhdh");
			media.setTitle(item);
			return media;
		}).collect(Collectors.toList());

		var category = new MediaCategoriesAdapter.Category(name, itemsList);
		categories.add(category);
		getEmptyAdapter().setEnabled(false);
	}
}