package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.ui.fragments.feeds.FeedViewHolder;
import com.mrboomdev.awery.ui.fragments.feeds.ListFeedViewHolder;
import com.mrboomdev.awery.ui.fragments.feeds.PagesFeedViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

public class MediaCategoriesAdapter extends RecyclerView.Adapter<FeedViewHolder> {
	public static final int VIEW_TYPE_PAGES = 1;
	public static final int VIEW_TYPE_LIST = 2;
	private final WeakHashMap<FeedViewHolder.Feed, Long> ids = new WeakHashMap<>();
	private final List<FeedViewHolder.Feed> categories = new ArrayList<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();

	@SuppressLint("NotifyDataSetChanged")
	public MediaCategoriesAdapter() {
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return Objects.requireNonNull(ids.get(categories.get(position)));
	}

	@Override
	public int getItemViewType(int position) {
		return switch(categories.get(position).getDisplayMode()) {
			case LIST_HORIZONTAL -> VIEW_TYPE_LIST;
			case SLIDES -> VIEW_TYPE_PAGES;
			case LIST_VERTICAL, GRID -> VIEW_TYPE_LIST; // TODO: Handle other display modes
		};
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setCategories(@NonNull List<FeedViewHolder.Feed> categories) {
		this.categories.clear();
		this.categories.addAll(categories);
		this.idGenerator.clear();

		for(var category : categories) {
			ids.put(category, idGenerator.getLong());
		}

		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void addCategory(FeedViewHolder.Feed category) {
		this.categories.add(category);
		this.ids.put(category, idGenerator.getLong());

		// Try fixing auto scrolling to bottom
		/*if(categories.size() <= 1) notifyDataSetChanged();
		else */notifyItemInserted(categories.size() - 1);
	}

	public void removeCategory(FeedViewHolder.Feed category) {
		var wasIndex = this.categories.indexOf(category);
		this.categories.remove(category);
		this.ids.remove(category);
		notifyItemRemoved(wasIndex);
	}

	@NonNull
	@Override
	public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return switch(viewType) {
			case VIEW_TYPE_PAGES -> PagesFeedViewHolder.create(parent);
			case VIEW_TYPE_LIST -> ListFeedViewHolder.create(parent);
			default -> throw new IllegalArgumentException("Unknown view type! " + viewType);
		};
	}

	@Override
	public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
		holder.bind(categories.get(position));
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}
}