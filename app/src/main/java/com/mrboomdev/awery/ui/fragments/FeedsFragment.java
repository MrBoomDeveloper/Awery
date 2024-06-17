package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setVerticalPadding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.LayoutHeaderMainBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.sdk.data.CatalogFilter;
import com.mrboomdev.awery.ui.activity.SearchActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.adapter.MediaCategoriesAdapter;
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
	private final MediaCategoriesAdapter rowsAdapter = new MediaCategoriesAdapter(),
			failedRowsAdapter = new MediaCategoriesAdapter();
	private final SingleViewAdapter.BindingSingleViewAdapter<EmptyView> emptyStateAdapter =
			SingleViewAdapter.fromBindingDynamic(parent -> new EmptyView(parent, false));
	private final Queue<CatalogFeed> pendingFeeds = new LinkedBlockingQueue<>(), loadingFeeds = new LinkedBlockingQueue<>();
	private List<CatalogFeed> feeds;
	private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView recycler;
	private long loadId;

	@Override
	@SuppressWarnings("unchecked")
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.feeds = (List<CatalogFeed>) requireArguments().getSerializable("feeds");
	}

	public void scrollToTop() {
		Objects.requireNonNull(recycler.getLayoutManager()).startSmoothScroll(new LinearSmoothScroller(requireContext()) {
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

			swipeRefreshLayout.setRefreshing(false);
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
		}, recycler));
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

					runOnUiThread(() -> {
						rowsAdapter.addCategory(new MediaCategoriesAdapter.Category(feed.title, catalogMedia));
						if(rowsAdapter.getItemCount() < 2) recycler.getAdapter().notifyDataSetChanged();
					}, recycler);

					tryToLoadNextFeed(feed, currentLoadId);
				}

				@Override
				public void onFailure(Throwable e) {
					Log.e(TAG, "Failed to load an feed!", e);

					if(!(feed.hideIfEmpty && e instanceof ZeroResultsException)) {
						runOnUiThread(() -> failedRowsAdapter.addCategory(
								new MediaCategoriesAdapter.Category(feed.title, e)), recycler);
					}

					tryToLoadNextFeed(feed, currentLoadId);
				}
			});
		} else {
			runOnUiThread(() -> failedRowsAdapter.addCategory(
					new MediaCategoriesAdapter.Category(feed.title,
							new ZeroResultsException("No extension provider was found!", 0) {
						@Override
						public String getTitle(@NonNull Context context) {
							return "Extension was not found!";
						}

						@Override
						public String getDescription(@NonNull Context context) {
							return "Please check your filters again. Maybe used extension was removed.";
						}
					})), recycler);

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
			}, recycler));
		}
	}

	@Nullable
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
		var frame = new FrameLayout(requireContext());

		swipeRefreshLayout = new SwipeRefreshLayout(requireContext());
		frame.addView(swipeRefreshLayout, MATCH_PARENT, MATCH_PARENT);

		recycler = new RecyclerView(requireContext());
		recycler.setClipToPadding(false);
		setVerticalPadding(recycler, dpPx(100));
		swipeRefreshLayout.addView(recycler, MATCH_PARENT, MATCH_PARENT);

		recycler.setLayoutManager(new LinearLayoutManager(
				requireContext(), LinearLayoutManager.VERTICAL, false));

		recycler.setAdapter(new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS).build(),
				rowsAdapter, emptyStateAdapter, failedRowsAdapter));

		var header = LayoutHeaderMainBinding.inflate(getLayoutInflater());
		frame.addView(header.getRoot(), MATCH_PARENT, WRAP_CONTENT);
		setupHeader(header, recycler);

		swipeRefreshLayout.setOnRefreshListener(() -> loadData(true));
		loadData(false);

		return frame;
	}


	private void setupHeader(@NonNull LayoutHeaderMainBinding header, RecyclerView recycler) {
		header.search.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SearchActivity.class);
			startActivity(intent);
		});

		header.settingsWrapper.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SettingsActivity.class);
			startActivity(intent);
		});

		setPadding(header.getRoot(), dpPx(16));

		setOnApplyUiInsetsListener(header.getRoot(), insets -> {
			setTopMargin(header.getRoot(), insets.top);
			setRightMargin(header.getRoot(), insets.right);
			setLeftMargin(header.getRoot(), insets.left);
			return false;
		});
	}
}