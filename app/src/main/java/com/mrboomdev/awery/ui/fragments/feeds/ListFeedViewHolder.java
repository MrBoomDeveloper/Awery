package com.mrboomdev.awery.ui.fragments.feeds;

import static com.mrboomdev.awery.app.AweryApp.getNavigationStyle;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
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

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.ItemListMediaCategoryBinding;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.activity.search.SearchActivity;
import com.mrboomdev.awery.ui.adapter.MediaCatalogAdapter;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.lang.ref.WeakReference;

public class ListFeedViewHolder extends FeedViewHolder {
	private static WeakReference<RecyclerView.RecycledViewPool> itemsPool;
	private final ItemListMediaCategoryBinding binding;
	private final MediaCatalogAdapter adapter;

	@NonNull
	@Contract("_ -> new")
	public static ListFeedViewHolder create(ViewGroup parent) {
		return new ListFeedViewHolder(ItemListMediaCategoryBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false), parent);
	}

	private ListFeedViewHolder(@NonNull ItemListMediaCategoryBinding binding, ViewGroup parent) {
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
				setLeftMargin(binding.header, dpPx(16) +
						(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL ? insets.left : 0));

				setRightMargin(binding.header, insets.right + dpPx(16));
			} else {
				setHorizontalMargin(binding.header, 0);
			}

			return true;
		}, parent);

		setOnApplyUiInsetsListener(binding.recycler, insets -> {
			if(isLandscape()) {
				setLeftPadding(binding.recycler, dpPx(32) +
						(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL ? insets.left : 0));

				setRightPadding(binding.recycler, insets.right + dpPx(32));
			} else {
				setHorizontalPadding(binding.recycler, dpPx(16));
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
				intent.putExtra(SearchActivity.EXTRA_FILTERS, (Serializable) feed.sourceFeed.filters);
				intent.putExtra(SearchActivity.EXTRA_LOADED_MEDIA, (Serializable) feed.getItems());
				getContext(v).startActivity(intent);
			});
		} else {
			binding.header.setClickable(false);
			binding.expand.setVisibility(View.GONE);
		}

		if(feed.getItems() == null || feed.getItems().isEmpty()) {
			if(feed.getThrowable() != null) {
				binding.errorMessage.setText(ExceptionDescriptor.print(
						ExceptionDescriptor.unwrap(feed.getThrowable()), getContext(binding)));
			} else {
				binding.errorMessage.setText(getContext(binding).getString(R.string.nothing_found));
			}

			binding.errorMessage.setVisibility(View.VISIBLE);
			binding.recycler.setVisibility(View.GONE);
		} else {
			binding.errorMessage.setVisibility(View.GONE);
			binding.recycler.setVisibility(View.VISIBLE);
		}
	}
}