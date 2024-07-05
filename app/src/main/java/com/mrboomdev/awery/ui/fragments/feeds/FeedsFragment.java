package com.mrboomdev.awery.ui.fragments.feeds;

import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.ScreenFeedBinding;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class FeedsFragment extends Fragment {
	private static final String TAG = "FeedsFragment";
	private static final int MAX_LOADING_FEEDS_AT_TIME = 5;
	private final Queue<CatalogFeed> pendingFeeds = new LinkedBlockingQueue<>();
	private final Queue<CatalogFeed> loadingFeeds = new LinkedBlockingQueue<>();
	private ScreenFeedBinding binding;
	private List<CatalogFeed> feeds;
	private boolean doSlidesExist;
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
	public void startLoading(boolean isReload) {
		scrollToTop();
		var currentLoadId = ++loadId;

		binding.swipeRefresher.setRefreshing(false);
		emptyStateAdapter.getBinding(EmptyView::startLoading);

		runOnUiThread(() -> {
			rowsAdapter.setCategories(Collections.emptyList());
			failedRowsAdapter.setCategories(Collections.emptyList());
			setContentBehindToolbarEnabled(false);

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
		}, this.binding.recycler);
	}

	private void loadFeed(@NonNull CatalogFeed feed, long currentLoadId) {
		var finishCallback = new ExtensionProvider.ResponseCallback<CatalogSearchResults<? extends CatalogMedia>>() {

			@SuppressLint("NotifyDataSetChanged")
			@Override
			public void onSuccess(CatalogSearchResults<? extends CatalogMedia> searchResults) {
				if(currentLoadId != loadId) return;

				var filtered = MediaUtils.filterMediaSync(searchResults);
				var filteredResults = CatalogSearchResults.of(filtered, searchResults.hasNextPage());

				if(filteredResults.isEmpty()) {
					onFailure(new ZeroResultsException("All results were filtered out.", R.string.no_media_found));
					return;
				}

				runOnUiThread(() -> {
					/* Only a single instance with an DisplayMode.SLIDES can be made,
					*  because else the app will look messy. */
					var displayMode = !canHaveOtherViewTypes() ? CatalogFeed.DisplayMode.LIST_HORIZONTAL
							: (rowsAdapter.getItemCount() != 0 ? (
									feed.displayMode == CatalogFeed.DisplayMode.SLIDES ? (doSlidesExist
											? CatalogFeed.DisplayMode.SLIDES
											: CatalogFeed.DisplayMode.LIST_HORIZONTAL) : feed.displayMode
					) : CatalogFeed.DisplayMode.SLIDES);

					if(displayMode == CatalogFeed.DisplayMode.SLIDES) {
						setContentBehindToolbarEnabled(true);
						doSlidesExist = true;
					}

					rowsAdapter.addCategory(new FeedViewHolder.Feed(feed, filteredResults, displayMode));

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
							new FeedViewHolder.Feed(feed, e)), binding.recycler);
				}

				tryToLoadNextFeed(feed, currentLoadId);
			}
		};

		try {
			var provider = ExtensionProvider.forGlobalId(feed.sourceManager, feed.extensionId, feed.sourceId);

			var context = getContext();
			if(context == null) return;

			var filters = new ArrayList<>(getFilters());

			if(feed.filters != null) {
				filters.addAll(feed.filters);
			}

			if(feed.sourceFeed != null) {
				filters.add(new SettingsItem(SettingsItemType.STRING, ExtensionProvider.FILTER_FEED, feed.sourceFeed));
			}

			var queryFilter = find(filters, filter -> Objects.equals(filter.getKey(), ExtensionProvider.FILTER_QUERY));

			if(queryFilter != null) {
				if(feed.filters == null) {
					feed.filters = new ArrayList<>(Collections.singletonList(queryFilter));
				} else {
					var found = find(feed.filters, filter -> Objects.equals(filter.getKey(), ExtensionProvider.FILTER_QUERY));
					if(found != null) feed.filters.remove(found);
					feed.filters.add(queryFilter);
				}
			}

			provider.searchMedia(context, filters, finishCallback);
		} catch(ExtensionNotInstalledException e) {
			Log.e(TAG, "Extension isn't installed, can't load the feed!", e);

			runOnUiThread(() -> failedRowsAdapter.addCategory(
					new FeedViewHolder.Feed(feed,
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

	@CallSuper
	public void setContentBehindToolbarEnabled(boolean isEnabled) {
		binding.headerWrapper.setBackground(null);

		useLayoutParams(binding.swipeRefresher, params -> params.setBehavior(isEnabled ? null :
				new AppBarLayout.ScrollingViewBehavior()), CoordinatorLayout.LayoutParams.class);
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
		} else if(pendingFeeds.isEmpty() && loadingFeeds.isEmpty()) {
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
		binding.swipeRefresher.setOnRefreshListener(() -> startLoading(true));
		binding.headerWrapper.addView(getHeader(binding.headerWrapper));

		binding.headerWrapper.addOnOffsetChangedListener((v, offset) ->
				binding.swipeRefresher.setEnabled(offset == 0));

		binding.recycler.setAdapter(new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.setIsolateViewTypes(true)
				.build(), rowsAdapter, emptyStateAdapter, failedRowsAdapter));

		/* Sometimes user may not be able to expand the toolbar at the top of list,
		*  so we manually do it for him. */
		binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				if(newState != RecyclerView.SCROLL_STATE_IDLE) return;

				var manager = Objects.requireNonNull(
						(LinearLayoutManager) recyclerView.getLayoutManager());

				if(manager.findFirstCompletelyVisibleItemPosition() == 0) {
					binding.headerWrapper.setExpanded(true, true);
				}
			}
		});

		if(loadOnStartup()) {
			startLoading(false);
		} else {
			emptyStateAdapter.getBinding(binding ->
					binding.setInfo(null, null));
		}

		return binding.getRoot();
	}

	protected abstract boolean canHaveOtherViewTypes();

	protected abstract List<SettingsItem> getFilters();

	protected abstract boolean loadOnStartup();

	protected abstract View getHeader(ViewGroup parent);

	@Nullable
	protected abstract File getCacheFile();
}