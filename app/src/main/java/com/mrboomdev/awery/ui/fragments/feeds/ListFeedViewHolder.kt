package com.mrboomdev.awery.ui.fragments.feeds;

import static com.mrboomdev.awery.app.App.getNavigationStyle;
import static com.mrboomdev.awery.app.App.isLandscape;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.databinding.FeedListBinding;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.activity.search.SearchActivity;
import com.mrboomdev.awery.ui.adapter.MediaCatalogAdapter;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.lang.ref.WeakReference;

public class ListFeedViewHolder extends FeedViewHolder {
	private static WeakReference<RecyclerView.RecycledViewPool> itemsPool;
	private final FeedListBinding binding;
	private final MediaCatalogAdapter adapter;

	@NonNull
	@Contract("_ -> new")
	public static ListFeedViewHolder create(ViewGroup parent) {
		return new ListFeedViewHolder(FeedListBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false), parent);
	}

	private ListFeedViewHolder(@NonNull FeedListBinding binding, ViewGroup parent) {
		super(binding.getRoot());
		this.binding = binding;

		adapter = new MediaCatalogAdapter();

		var pool = itemsPool == null ? null : itemsPool.get();

		if(pool == null) {
			pool = new RecyclerView.RecycledViewPool();
			itemsPool = new WeakReference<>(pool);
		}

		binding.header.setOnClickListener(v -> binding.expand.performClick());
		binding.recycler.setRecycledViewPool(pool);
		binding.recycler.setAdapter(adapter);

		setOnApplyUiInsetsListener(binding.header, insets -> {
			if(isLandscape()) {
				setLeftMargin(binding.header, dpPx(binding.header, 16) +
						(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL ? insets.left : 0));

				setRightMargin(binding.header, insets.right + dpPx(binding.header, 16));
			} else {
				setHorizontalMargin(binding.header, 0);
			}

			return true;
		}, parent);

		setOnApplyUiInsetsListener(binding.recycler, insets -> {
			if(isLandscape()) {
				setLeftPadding(binding.recycler, dpPx(binding.recycler, 32) +
						(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL ? insets.left : 0));

				setRightPadding(binding.recycler, insets.right + dpPx(binding.recycler, 32));
			} else {
				setHorizontalPadding(binding.recycler, dpPx(binding.recycler, 16));
			}

			return true;
		}, parent);
	}

	@Override
	public void bind(@NonNull Feed feed) {
		binding.title.setText(feed.sourceFeed.title);
		adapter.setItems(feed.getItems());

		if(feed.getItems() instanceof CatalogSearchResults<?> searchResults && searchResults.hasNextPage()) {
			binding.expand.setVisibility(View.VISIBLE);
			binding.header.setClickable(true);

			binding.expand.setOnClickListener(v -> {
				var intent = new Intent(getContext(v), SearchActivity.class);
				intent.putExtra(SearchActivity.EXTRA_GLOBAL_PROVIDER_ID, feed.sourceFeed.getProviderGlobalId());
				intent.putExtra(SearchActivity.EXTRA_FILTERS, feed.sourceFeed.filters);
				intent.putExtra(SearchActivity.EXTRA_LOADED_MEDIA, (Serializable) feed.getItems());
				getContext(v).startActivity(intent);
			});
		} else {
			binding.header.setClickable(false);
			binding.expand.setVisibility(View.GONE);
			binding.expand.setOnClickListener(null);
		}
	}
}