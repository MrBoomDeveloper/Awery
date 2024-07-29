package com.mrboomdev.awery.ui.fragments.feeds;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;

import org.jetbrains.annotations.Contract;

import java.util.List;

public abstract class FeedViewHolder extends RecyclerView.ViewHolder {

	public FeedViewHolder(@NonNull View itemView) {
		super(itemView);
	}

	public abstract void bind(Feed feed);

	public static class Feed {
		public final CatalogFeed sourceFeed;
		public boolean isLoading;
		private final CatalogFeed.DisplayMode displayMode;
		private final CatalogSearchResults<? extends CatalogMedia> items;
		private final Runnable reloadCallback;
		private final Throwable throwable;

		public List<? extends CatalogMedia> getItems() {
			return items;
		}

		@Nullable
		public Throwable getThrowable() {
			return throwable;
		}

		@Nullable
		public Runnable getReloadCallback() {
			return reloadCallback;
		}

		public CatalogFeed.DisplayMode getDisplayMode() {
			return displayMode;
		}

		public Feed(
				@NonNull CatalogFeed sourceFeed,
				CatalogSearchResults<? extends CatalogMedia> items,
				Throwable throwable,
				Runnable reloadCallback,
				CatalogFeed.DisplayMode displayMode
		) {
			this.sourceFeed = sourceFeed;
			this.items = items;
			this.throwable = throwable;
			this.reloadCallback = reloadCallback;
			this.displayMode = displayMode;
		}

		public Feed(
				@NonNull CatalogFeed sourceFeed,
				CatalogSearchResults<? extends CatalogMedia> items,
				CatalogFeed.DisplayMode displayMode
		) {
			this(sourceFeed, items, null, null, displayMode);
		}

		public Feed(@NonNull CatalogFeed sourceFeed, CatalogSearchResults<? extends CatalogMedia> items) {
			this(sourceFeed, items, null, null, sourceFeed.displayMode);
		}

		@Contract(pure = true)
		public Feed(@NonNull CatalogFeed sourceFeed, Throwable throwable, Runnable reloadCallback) {
			this(sourceFeed, CatalogSearchResults.empty(), throwable, reloadCallback, CatalogFeed.DisplayMode.LIST_HORIZONTAL);
		}
	}
}