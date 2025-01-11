package com.mrboomdev.awery.ui.mobile.screens.catalog

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrboomdev.awery.extensions.data.CatalogFeed
import com.mrboomdev.awery.ui.mobile.screens.catalog.feeds.FailedFeedViewHolder
import com.mrboomdev.awery.ui.mobile.screens.catalog.feeds.FeedViewHolder
import com.mrboomdev.awery.ui.mobile.screens.catalog.feeds.FeedViewHolder.Feed
import com.mrboomdev.awery.ui.mobile.screens.catalog.feeds.ListFeedViewHolder
import com.mrboomdev.awery.ui.mobile.screens.catalog.feeds.PagesFeedViewHolder
import com.mrboomdev.awery.utils.UniqueIdGenerator
import java.util.WeakHashMap

class MediaCategoriesAdapter : RecyclerView.Adapter<FeedViewHolder>() {
	private val ids = WeakHashMap<Feed, Long?>()
	private val feeds: MutableList<Feed> = ArrayList()
	private val idGenerator = UniqueIdGenerator()

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return ids[feeds[position]]!!
	}

	override fun getItemViewType(position: Int): Int {
		val feed = feeds[position]

		if(feed.items == null || feed.items.isEmpty()) {
			return VIEW_TYPE_ERROR
		}

		return when(feed.displayMode) {
			CatalogFeed.DisplayMode.LIST_HORIZONTAL -> VIEW_TYPE_LIST
			CatalogFeed.DisplayMode.SLIDES -> VIEW_TYPE_PAGES
			CatalogFeed.DisplayMode.LIST_VERTICAL, CatalogFeed.DisplayMode.GRID -> VIEW_TYPE_LIST
			else -> VIEW_TYPE_LIST
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	fun setFeeds(feeds: List<Feed>) {
		this.feeds.clear()
		this.feeds.addAll(feeds)
		idGenerator.reset()

		for(category in feeds) {
			ids[category] = idGenerator.long
		}

		notifyDataSetChanged()
	}

	fun updateFeed(feed: Feed) {
		updateFeed(feed, feed)
	}

	fun updateFeed(oldFeed: Feed, newFeed: Feed) {
		val index = feeds.indexOf(oldFeed)
		val id = ids[oldFeed]

		if(index == -1) {
			throw NoSuchElementException()
		}

		feeds[index] = newFeed
		ids.remove(oldFeed)
		ids[newFeed] = id

		notifyItemChanged(index)
	}

	fun addFeed(feed: Feed) {
		feeds.add(feed)
		ids[feed] = idGenerator.long
		notifyItemInserted(feeds.size - 1)
	}

	@SuppressLint("NotifyDataSetChanged")
	fun addFeed(feed: Feed, index: Int) {
		feeds.add(index, feed)
		ids[feed] = idGenerator.long
		notifyItemInserted(index)
	}

	fun removeFeed(feed: Feed) {
		val wasIndex = feeds.indexOf(feed)
		feeds.remove(feed)
		ids.remove(feed)
		notifyItemRemoved(wasIndex)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
		return when(viewType) {
			VIEW_TYPE_PAGES -> PagesFeedViewHolder.create(parent)
			VIEW_TYPE_LIST -> ListFeedViewHolder.create(parent)
			VIEW_TYPE_ERROR -> FailedFeedViewHolder.create(parent)
			else -> throw IllegalArgumentException("Unknown view type! $viewType")
		}
	}

	override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
		holder.bind(feeds[position])
	}

	override fun getItemCount(): Int {
		return feeds.size
	}

	companion object {
		const val VIEW_TYPE_PAGES: Int = 1
		const val VIEW_TYPE_LIST: Int = 2
		const val VIEW_TYPE_ERROR: Int = 3
	}
}