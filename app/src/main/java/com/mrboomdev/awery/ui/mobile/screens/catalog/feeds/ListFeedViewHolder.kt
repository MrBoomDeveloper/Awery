package com.mrboomdev.awery.ui.mobile.screens.catalog.feeds

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.navigationStyle
import com.mrboomdev.awery.databinding.FeedListBinding
import com.mrboomdev.awery.extensions.data.CatalogSearchResults
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.mobile.screens.search.SearchActivity
import com.mrboomdev.awery.ui.mobile.screens.catalog.MediaCatalogAdapter
import com.mrboomdev.awery.util.WeakLazy
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.inflater
import com.mrboomdev.awery.util.extensions.leftMargin
import com.mrboomdev.awery.util.extensions.leftPadding
import com.mrboomdev.awery.util.extensions.rightMargin
import com.mrboomdev.awery.util.extensions.rightPadding
import com.mrboomdev.awery.util.extensions.setHorizontalMargin
import com.mrboomdev.awery.util.extensions.setHorizontalPadding
import com.mrboomdev.awery.util.extensions.startActivity
import org.jetbrains.annotations.Contract

class ListFeedViewHolder private constructor(
	private val binding: FeedListBinding,
	parent: ViewGroup
) : FeedViewHolder(binding.root) {
	private val adapter = MediaCatalogAdapter()

	init {
		binding.header.setOnClickListener { binding.expand.performClick() }
		binding.recycler.setRecycledViewPool(itemsPool)
		binding.recycler.adapter = adapter

		binding.header.applyInsets(UI_INSETS, { view, insets ->
			if(isLandscape) {
				view.leftMargin = view.dpPx(16f) + (if(
					navigationStyle != AwerySettings.NavigationStyle_Values.MATERIAL
				) insets.left else 0)

				binding.header.rightMargin = insets.right + view.dpPx(16f)
			} else {
				binding.header.setHorizontalMargin(0)
			}

			true
		}, parent)

		binding.recycler.applyInsets(UI_INSETS, { view, insets ->
			if(isLandscape) {
				view.leftPadding = view.dpPx(32f) + (if(
					navigationStyle != AwerySettings.NavigationStyle_Values.MATERIAL
				) insets.left else 0)

				view.rightPadding = insets.right + view.dpPx(32f)
			} else {
				view.setHorizontalPadding(view.dpPx(16f))
			}

			true
		}, parent)
	}

	override fun bind(feed: Feed) {
		binding.title.text = feed.sourceFeed.title
		adapter.setItems(feed.items)

		feed.items.let { items ->
			if(items is CatalogSearchResults<*> && items.hasNextPage()) {
				binding.expand.visibility = View.VISIBLE
				binding.header.isClickable = true

				binding.expand.setOnClickListener { v ->
					v.context.startActivity(
						SearchActivity::class, args = SearchActivity.Extras(
						sourceGlobalId = feed.sourceFeed.providerGlobalId,
						filters = feed.sourceFeed.filters,
						preloadedItems = feed.items
					))
				}
			} else {
				binding.header.isClickable = false
				binding.expand.visibility = View.GONE
				binding.expand.setOnClickListener(null)
			}
		}
	}

	companion object {
		private val itemsPool: RecycledViewPool by WeakLazy {
			RecycledViewPool()
		}

		@JvmStatic
		@Contract("_ -> new")
		fun create(parent: ViewGroup): ListFeedViewHolder {
			return ListFeedViewHolder(FeedListBinding.inflate(
				parent.context.inflater, parent, false
			), parent)
		}
	}
}