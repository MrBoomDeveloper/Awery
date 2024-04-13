package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.db.DBCatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.util.UniqueIdGenerator;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends MediaCatalogListsFragment {
	private final MediaCategoriesAdapter categoriesAdapter = new MediaCategoriesAdapter();
	private final List<MediaCategoriesAdapter.Category> categories = new ArrayList<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
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
			runOnUiThread(() -> {
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
		categories.clear();
		idGenerator.clear();
		categoriesAdapter.notifyDataSetChanged();

		setEmptyData(true);
		getEmptyAdapter().setEnabled(true);

		new Thread(() -> {
			var db = AweryApp.getDatabase();
			var lists = db.getListDao().getAll();
			var categories = new ArrayList<MediaCategoriesAdapter.Category>();

			for(var list : lists) {
				if(list.getId().equals(AweryApp.CATALOG_LIST_BLACKLIST)) continue;

				var mediasProgresses = db.getMediaProgressDao().getAllFromList(list.getId());
				if(mediasProgresses.isEmpty()) continue;

				var dbMediaList = db.getMediaDao().getAllByIds(stream(mediasProgresses)
						.map(CatalogMediaProgress::getGlobalId).toList());

				if(dbMediaList.isEmpty()) continue;

				var mediaList = stream(dbMediaList)
						.map(DBCatalogMedia::toCatalogMedia)
						.toList();

				var category = new MediaCategoriesAdapter.Category(list.getName(), mediaList);
				category.id = idGenerator.getLong();
				categories.add(category);
			}

			loadCategories(categories, wasLoadId);
		}).start();
	}

	@SuppressLint("NotifyDataSetChanged")
	private void loadCategories(List<MediaCategoriesAdapter.Category> categories, long loadId) {
		if(loadId != this.loadId) return;

		runOnUiThread(() -> {
			if(loadId != this.loadId) return;

			if(categories.isEmpty()) {
				setEmptyData(false,
						getString(R.string.empty_library_title),
						getString(R.string.empty_library_message));

				return;
			}

			this.categories.addAll(categories);
			getEmptyAdapter().setEnabled(false);
			categoriesAdapter.notifyDataSetChanged();

			// We do this to prevent screen from being scrolling a little bit
			runDelayed(() -> {
				if(loadId != this.loadId) return;
				getBinding().catalogCategories.scrollToPosition(0);
			}, 100);
		});
	}
}