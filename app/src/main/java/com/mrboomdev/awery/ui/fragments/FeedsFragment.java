package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryApp.getNavigationStyle;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearSmoothScroller;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.ScreenFeedBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.activity.SearchActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class FeedsFragment extends Fragment {
	private static final String TAG = "FeedsFragment";
	private static final int MAX_LOADING_FEEDS_AT_TIME = 5;
	private final Queue<CatalogFeed> pendingFeeds = new LinkedBlockingQueue<>();
	private final Queue<CatalogFeed> loadingFeeds = new LinkedBlockingQueue<>();
	private ScreenFeedBinding binding;
	private List<CatalogFeed> feeds;
	private long loadId;

	private final MediaCategoriesAdapter rowsAdapter = new MediaCategoriesAdapter(),
			failedRowsAdapter = new MediaCategoriesAdapter();

	private final SingleViewAdapter.BindingSingleViewAdapter<EmptyView> emptyStateAdapter =
			SingleViewAdapter.fromBindingDynamic(parent -> new EmptyView(parent, false));

	@Override
	@SuppressWarnings("unchecked")
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.feeds = (List<CatalogFeed>) requireArguments().getSerializable("feeds");
	}

	public void scrollToTop() {
		if(binding == null) return;

		var layoutManager = binding.recycler.getLayoutManager();
		if(layoutManager == null) return;

		layoutManager.startSmoothScroll(new LinearSmoothScroller(requireContext()) {
			{ setTargetPosition(0); }

			@Override
			protected int getVerticalSnapPreference() {
				return LinearSmoothScroller.SNAP_TO_START;
			}

			@Override
			protected int getHorizontalSnapPreference() {
				return LinearSmoothScroller.SNAP_TO_START;
			}
		});
	}

	@SuppressLint("NotifyDataSetChanged")
	public void loadData(boolean isReload) {
		scrollToTop();
		var currentLoadId = ++loadId;

		emptyStateAdapter.getBinding(binding -> runOnUiThread(() -> {
			binding.startLoading();
			rowsAdapter.setCategories(Collections.emptyList());
			failedRowsAdapter.setCategories(Collections.emptyList());

			this.binding.swipeRefresher.setRefreshing(false);
			emptyStateAdapter.notifyDataSetChanged();

			new Thread(() -> {
				var processedFeeds = new ArrayList<>(CatalogFeed.processFeeds(feeds));
				Collections.shuffle(processedFeeds);

				if(processedFeeds.isEmpty()) {
					tryToLoadNextFeed(null, currentLoadId);
				} else if(processedFeeds.size() <= MAX_LOADING_FEEDS_AT_TIME) {
					loadingFeeds.addAll(processedFeeds);
				} else {
					loadingFeeds.addAll(processedFeeds.subList(0, MAX_LOADING_FEEDS_AT_TIME));
					pendingFeeds.addAll(processedFeeds.subList(MAX_LOADING_FEEDS_AT_TIME, processedFeeds.size()));
				}

				for(var loadingFeed : loadingFeeds) {
					loadFeed(loadingFeed, currentLoadId);
				}
			}).start();
		}, this.binding.recycler));
	}

	private void loadFeed(@NonNull CatalogFeed feed, long currentLoadId) {
		var requiredFeatures = List.of(ExtensionProvider.FEATURE_MEDIA_SEARCH);

		var extensions = ExtensionsFactory.getManager(feed.sourceManager)
				.getExtensions(Extension.FLAG_WORKING);

		var provider = stream(extensions)
				.flatMap(NiceUtils::stream)
				.map(ext -> ext.getProviders(requiredFeatures))
				.flatMap(NiceUtils::stream)
				.filter(extProvider -> extProvider.getId().equals(feed.sourceId))
				.findAny().orElse(null);

		if(provider != null) {
			var context = getContext();
			if(context == null) return;

			var filters = List.of(
					new SettingsItem(SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0),
					new SettingsItem(SettingsItemType.STRING, ExtensionProvider.FILTER_FEED, feed.sourceFeed)
			);

			if(feed.filters != null && !feed.filters.isEmpty()) {
				filters = new ArrayList<>(filters);
				filters.addAll(feed.filters);
			}

			provider.searchMedia(context, filters, new ExtensionProvider.ResponseCallback<>() {
				@SuppressLint("NotifyDataSetChanged")
				@Override
				public void onSuccess(CatalogSearchResults<? extends CatalogMedia> catalogMedia) {
					if(currentLoadId != loadId) return;

					var filtered = MediaUtils.filterMediaSync(catalogMedia);
					var filteredResults = CatalogSearchResults.of(filtered, catalogMedia.hasNextPage());

					runOnUiThread(() -> {
						rowsAdapter.addCategory(new MediaCategoriesAdapter.Category(feed, filteredResults));

						// I hope it'll don't do anything bad
						if(rowsAdapter.getItemCount() < 2) {
							Objects.requireNonNull(binding.recycler.getAdapter()).notifyDataSetChanged();
						}
					}, binding.recycler);

					tryToLoadNextFeed(feed, currentLoadId);
				}

				@Override
				public void onFailure(Throwable e) {
					Log.e(TAG, "Failed to load an feed!", e);

					if(!(feed.hideIfEmpty && e instanceof ZeroResultsException)) {
						runOnUiThread(() -> failedRowsAdapter.addCategory(
								new MediaCategoriesAdapter.Category(feed, e)), binding.recycler);
					}

					tryToLoadNextFeed(feed, currentLoadId);
				}
			});
		} else {
			runOnUiThread(() -> failedRowsAdapter.addCategory(
					new MediaCategoriesAdapter.Category(feed,
							new ZeroResultsException("No extension provider was found!", 0) {
						@Override
						public String getTitle(@NonNull Context context) {
							return "Extension was not found!";
						}

						@Override
						public String getDescription(@NonNull Context context) {
							return "Please check your filters again. Maybe used extension was removed.";
						}
					})), binding.recycler);

			tryToLoadNextFeed(feed, currentLoadId);
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	private void tryToLoadNextFeed(@Nullable CatalogFeed loadedFeed, long currentLoadId) {
		if(currentLoadId != this.loadId) return;

		if(loadedFeed != null) {
			loadingFeeds.remove(loadedFeed);
		}

		var nextFeed = pendingFeeds.poll();

		if(nextFeed != null) {
			loadingFeeds.add(nextFeed);
			loadFeed(nextFeed, currentLoadId);
		} else if(pendingFeeds.isEmpty()) {
			emptyStateAdapter.getBinding(binding -> runOnUiThread(() -> {
				binding.setInfo(R.string.you_reached_end, R.string.you_reached_end_description);
				emptyStateAdapter.notifyDataSetChanged();
			}, this.binding.recycler));
		}
	}

	@Nullable
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
		binding = ScreenFeedBinding.inflate(inflater, container, false);
		binding.swipeRefresher.setOnRefreshListener(() -> loadData(true));
		setupHeader();

		binding.headerWrapper.addOnOffsetChangedListener((v, offset) ->
				binding.swipeRefresher.setEnabled(offset == 0));

		binding.recycler.setAdapter(new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS).build(),
				rowsAdapter, emptyStateAdapter, failedRowsAdapter));

		loadData(false);
		return binding.getRoot();
	}

	private void setupHeader() {
		binding.search.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SearchActivity.class);
			startActivity(intent);
		});

		binding.settingsWrapper.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SettingsActivity.class);
			startActivity(intent);
		});

		setOnApplyUiInsetsListener(binding.header, insets -> {
			setTopPadding(binding.header, insets.top + dpPx(16));
			setRightPadding(binding.header, insets.right + dpPx(16));

			if(isLandscape()) {
				if(getNavigationStyle() == AwerySettings.NavigationStyle_Values.MATERIAL) {
					setLeftPadding(binding.header, dpPx(16));
				} else {
					setLeftPadding(binding.header, dpPx(16) + insets.left);
				}
			} else {
				setLeftPadding(binding.header, dpPx(16));
			}

			return false;
		});
	}
}