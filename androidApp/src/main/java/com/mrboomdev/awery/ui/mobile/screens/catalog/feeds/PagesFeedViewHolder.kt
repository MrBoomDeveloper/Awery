package com.mrboomdev.awery.ui.mobile.screens.catalog.feeds

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.navigationStyle
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.app.theme.ThemeManager
import com.mrboomdev.awery.databinding.FeedFeaturedItemBinding
import com.mrboomdev.awery.databinding.FeedFeaturedWrapperBinding
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.mobile.dialogs.MediaActionsDialog
import com.mrboomdev.awery.ui.mobile.dialogs.MediaBookmarkDialog
import com.mrboomdev.awery.ui.mobile.screens.media.MediaActivity
import com.mrboomdev.awery.util.MediaUtils
import com.mrboomdev.awery.utils.UniqueIdGenerator
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.leftMargin
import com.mrboomdev.awery.util.extensions.limit
import com.mrboomdev.awery.util.extensions.rightMargin
import com.mrboomdev.awery.util.extensions.topMargin
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.inflater
import org.jetbrains.annotations.Contract
import java.util.WeakHashMap
import kotlin.math.min

class PagesFeedViewHolder private constructor(
	private val binding: FeedFeaturedWrapperBinding, parent: ViewGroup
) : FeedViewHolder(binding.root) {
	private val ids = WeakHashMap<CatalogMedia, Long>()
	private val adapter: PagerAdapter = PagerAdapter()
	private val idGenerator = UniqueIdGenerator()
	private var feed: Feed? = null

	init {
		binding.pager.adapter = adapter

		binding.pageIndicator.applyInsets(UI_INSETS, { view, insets ->
			if(isLandscape) {
				binding.pageIndicator.rightMargin = insets.right + view.dpPx(16f)
			} else {
				binding.pageIndicator.rightMargin = 0
			}

			true
		}, parent)
	}

	@SuppressLint("NotifyDataSetChanged")
	override fun bind(feed: Feed) {
		this.feed = feed
		idGenerator.reset()

		if(feed.items != null) {
			for(item in feed.items) {
				ids[item] = idGenerator.long
			}
		}

		binding.pager.setCurrentItem(0, false)
		adapter.notifyDataSetChanged()
	}

	private inner class PagerAdapter : RecyclerView.Adapter<PagerViewHolder>() {
		init {
			setHasStableIds(true)
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
			val binding = FeedFeaturedItemBinding.inflate(parent.context.inflater, parent, false)
			val holder = PagerViewHolder(binding)

			binding.root.setOnClickListener {
				parent.context.startActivity(parent.context.buildIntent(MediaActivity::class, MediaActivity.Extras(
					media = holder.item!!, action = MediaActivity.Action.INFO)))
			}

			binding.watch.setOnClickListener {
				parent.context.startActivity(parent.context.buildIntent(MediaActivity::class, MediaActivity.Extras(
					media = holder.item!!, action = MediaActivity.Action.WATCH)))
			}

			binding.bookmark.setOnClickListener {
				MediaBookmarkDialog(holder.item!!).show(parent.context)
			}

			binding.root.setOnLongClickListener {
				val media = holder.item!!
				val index = feed!!.items.indexOf(media)
				val dialog = MediaActionsDialog(media)

				dialog.updateCallback = {
					MediaUtils.isMediaFiltered(media) { isFiltered ->
						if(!isFiltered!!) return@isMediaFiltered

						runOnUiThread {
							val was = feed!!.items.removeAt(index)
							notifyItemRemoved(index)
							ids.remove(was)
						}
					}
				}

				dialog.show(parent.context)
				true
			}

			binding.leftSideBarrier.applyInsets(UI_INSETS, { view, insets ->
				if(isLandscape) {
					view.leftMargin = view.dpPx(32f) + (if(
						navigationStyle != AwerySettings.NavigationStyleValue.MATERIAL
					) insets.left else 0)
				} else {
					view.rightMargin = 0
				}

				true
			}, parent)

			binding.rightSideBarrier.applyInsets(UI_INSETS, { view, insets ->
				view.rightMargin = insets.right
				true
			}, parent)

			binding.topSideBarrier.applyInsets(UI_INSETS, { view, insets ->
				view.topMargin = insets.top
				true
			}, parent)

			return holder
		}

		override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
			holder.bind(feed!!.items[position])
		}

		override fun getItemId(position: Int): Long {
			return ids[feed!!.items[position]]!!
		}

		override fun getItemCount(): Int {
			if(feed == null) {
				return 0
			}

			return min(feed!!.items.size.toDouble(), MAX_ITEMS.toDouble()).toInt()
		}
	}

	private class PagerViewHolder(private val binding: FeedFeaturedItemBinding) : RecyclerView.ViewHolder(binding.root) {
		var item: CatalogMedia? = null
			private set

		@SuppressLint("SetTextI18n")
		fun bind(item: CatalogMedia) {
			binding.title.text = item.title

			var description = if(item.description == null) null else Html.fromHtml(
				item.description, Html.FROM_HTML_MODE_COMPACT
			).toString().trim { it <= ' ' }

			while(description != null && description.contains("\n\n")) {
				description = description.replace("\n\n".toRegex(), "\n")
			}

			binding.description.text = description

			when(item.type ?: CatalogMedia.Type.TV) {
				CatalogMedia.Type.TV, CatalogMedia.Type.MOVIE -> {
					binding.watch.text = i18n(Res.string.watch_now)
					binding.watch.setIconResource(R.drawable.ic_play_filled)
				}

				CatalogMedia.Type.BOOK, CatalogMedia.Type.POST -> {
					binding.watch.text = i18n(Res.string.read_now)
					binding.watch.setIconResource(R.drawable.ic_book_filled)
				}
			}

			if(item.averageScore != null) {
				binding.metaSeparator.visibility = View.VISIBLE
				binding.status.visibility = View.VISIBLE
				binding.status.text = item.averageScore.toString() + "/10"
			} else {
				binding.metaSeparator.visibility = View.GONE
				binding.status.visibility = View.GONE
			}

			binding.tags.text = (item.genres?.asList() ?: item.tags?.map { it.name })
				?.limit(3)?.joinToString(", ") ?: ""

			binding.poster.setImageDrawable(null)
			binding.banner.setImageDrawable(null)

			if(ThemeManager.isDarkModeEnabled) {
				binding.metaSeparator.setShadowLayer(1f, 0f, 0f, Color.BLACK)
				binding.tags.setShadowLayer(1f, 0f, 0f, Color.BLACK)
				binding.status.setShadowLayer(1f, 0f, 0f, Color.BLACK)
				binding.title.setShadowLayer(3f, 0f, 0f, Color.BLACK)
				binding.description.setShadowLayer(2f, 0f, 0f, Color.BLACK)
			} else {
				binding.metaSeparator.setShadowLayer(0f, 0f, 0f, 0)
				binding.tags.setShadowLayer(0f, 0f, 0f, 0)
				binding.status.setShadowLayer(0f, 0f, 0f, 0)
				binding.title.setShadowLayer(0f, 0f, 0f, 0)
				binding.description.setShadowLayer(0f, 0f, 0f, 0)
			}

			Glide.with(binding.root)
				.load(item.poster ?: item.banner)
				.transition(DrawableTransitionOptions.withCrossFade())
				.into(binding.poster)

			Glide.with(binding.root)
				.load(item.banner ?: item.poster)
				.transition(DrawableTransitionOptions.withCrossFade())
				.centerCrop()
				.into(binding.banner)

			this.item = item
		}
	}

	companion object {
		private const val MAX_ITEMS = 10
		@JvmStatic
		@Contract("_ -> new")
		fun create(parent: ViewGroup): PagesFeedViewHolder {
			return PagesFeedViewHolder(
				FeedFeaturedWrapperBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				), parent
			)
		}
	}
}