package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.ui.fragments.feeds.FailedFeedViewHolder;
import com.mrboomdev.awery.ui.fragments.feeds.FeedViewHolder;
import com.mrboomdev.awery.ui.fragments.feeds.ListFeedViewHolder;
import com.mrboomdev.awery.ui.fragments.feeds.PagesFeedViewHolder;
import com.mrboomdev.awery.util.UniqueIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.WeakHashMap;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<FeedViewHolder> {
	public static final int VIEW_TYPE_PAGES = 1;
	public static final int VIEW_TYPE_LIST = 2;
	public static final int VIEW_TYPE_ERROR = 3;
	private final WeakHashMap<FeedViewHolder.Feed, Long> ids = new WeakHashMap<>();
	private final List<FeedViewHolder.Feed> feeds = new ArrayList<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();

	@SuppressLint("NotifyDataSetChanged")
	public MediaCategoriesAdapter() {
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return Objects.requireNonNull(ids.get(feeds.get(position)));
	}

	@Override
	public int getItemViewType(int position) {
		var feed = feeds.get(position);

		if(feed.getItems() == null || feed.getItems().isEmpty()) {
			return VIEW_TYPE_ERROR;
		}

		return switch(feed.getDisplayMode()) {
			case LIST_HORIZONTAL -> VIEW_TYPE_LIST;
			case SLIDES -> VIEW_TYPE_PAGES;
			case LIST_VERTICAL, GRID -> VIEW_TYPE_LIST; // TODO: Handle other display modes
		};
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setFeeds(@NonNull List<FeedViewHolder.Feed> feeds) {
		this.feeds.clear();
		this.feeds.addAll(feeds);
		this.idGenerator.reset();

		for(var category : feeds) {
			ids.put(category, idGenerator.getLong());
		}

		notifyDataSetChanged();
	}

	public void updateFeed(FeedViewHolder.Feed feed) {
		updateFeed(feed, feed);
	}

	public void updateFeed(FeedViewHolder.Feed oldFeed, FeedViewHolder.Feed newFeed) {
		var index = feeds.indexOf(oldFeed);
		var id = ids.get(oldFeed);

		if(index == -1) {
			throw new NoSuchElementException();
		}

		feeds.set(index, newFeed);
		ids.remove(oldFeed);
		ids.put(newFeed, id);

		notifyItemChanged(index);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void addFeed(FeedViewHolder.Feed feed) {
		this.feeds.add(feed);
		this.ids.put(feed, idGenerator.getLong());

		// Try fixing auto scrolling to bottom
		/*if(categories.size() <= 1) notifyDataSetChanged();
		else */notifyItemInserted(feeds.size() - 1);
	}

	public void removeFeed(FeedViewHolder.Feed feed) {
		var wasIndex = this.feeds.indexOf(feed);
		this.feeds.remove(feed);
		this.ids.remove(feed);
		notifyItemRemoved(wasIndex);
	}

	@NonNull
	@Override
	public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return switch(viewType) {
			case VIEW_TYPE_PAGES -> PagesFeedViewHolder.create(parent);
			case VIEW_TYPE_LIST -> ListFeedViewHolder.create(parent);
			case VIEW_TYPE_ERROR -> FailedFeedViewHolder.create(parent);
			default -> throw new IllegalArgumentException("Unknown view type! " + viewType);
		};
	}

	@Override
	public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
		holder.bind(feeds.get(position));
	}

	@Override
	public int getItemCount() {
		return feeds.size();
	}
}