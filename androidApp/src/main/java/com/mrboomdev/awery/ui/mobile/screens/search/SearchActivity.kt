package com.mrboomdev.awery.ui.mobile.screens.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.data.settings.SettingsItemType
import com.mrboomdev.awery.data.settings.SettingsList
import com.mrboomdev.awery.databinding.GridMediaCatalogBinding
import com.mrboomdev.awery.databinding.ScreenSearchBinding
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.extensions.data.CatalogSearchResults
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.mobile.screens.media.MediaActivity
import com.mrboomdev.awery.ui.mobile.dialogs.FiltersDialog
import com.mrboomdev.awery.ui.mobile.dialogs.MediaActionsDialog
import com.mrboomdev.awery.util.MediaUtils
import com.mrboomdev.awery.util.Selection
import com.mrboomdev.awery.utils.UniqueIdGenerator
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.leftMargin
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.rightMargin
import com.mrboomdev.awery.util.extensions.topMargin
import com.mrboomdev.awery.util.extensions.useLayoutParams
import com.mrboomdev.awery.ui.mobile.components.EmptyStateView
import com.mrboomdev.awery.util.exceptions.explain
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter.BindingSingleViewAdapter
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.dpPx
import com.mrboomdev.awery.utils.inflater
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import com.mrboomdev.safeargsnext.util.asSafeArgs
import com.mrboomdev.safeargsnext.util.putSafeArgs
import java.util.Objects
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.find
import kotlin.collections.listOf
import kotlin.collections.set
import kotlin.collections.setOf

class SearchActivity : AppCompatActivity(), SafeArgsActivity<SearchActivity.Extras> {
	private lateinit var filters: SettingsList
	private lateinit var items: MutableList<CatalogMedia>
	private lateinit var binding: ScreenSearchBinding
	private val ids = WeakHashMap<CatalogMedia?, Long>()
	private val adapter: Adapter = Adapter()
	private val idGenerator = UniqueIdGenerator()
	private var loadingAdapter: BindingSingleViewAdapter<EmptyStateView>? = null
	private var source: ExtensionProvider? = null
	private var didReachedEnd = false
	private var searchId = 0

	private var queryFilter = SettingsItem(
		SettingsItemType.STRING, ExtensionProvider.FILTER_QUERY
	)

	private var pageFilter = SettingsItem(
		SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0
	)

	data class Extras(
		var filters: SettingsList? = null,
		val action: Action? = null,
		val queryTag: String? = null,
		val preloadedItems: List<CatalogMedia>? = null,
		val sourceGlobalId: String? = null
	)

	data class SavedState(
		var filters: SettingsList,
		var items: MutableList<CatalogMedia>,
		var didReachedEnd: Boolean
	)

	enum class Action {
		SEARCH_BY_TAG,
		PICK_MEDIA
	}

	/** We initially set this value to "true" so that list won't try
	 *  to load anything because we haven't typed anything yet. */
	private var isLoading = true

	@SuppressLint("NotifyDataSetChanged")
	override fun onCreate(savedInstanceState: Bundle?) {
		applyTheme()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		savedInstanceState?.asSafeArgs<SavedState>().also {
			filters = it?.filters ?: SettingsList()
			items = it?.items ?: ArrayList()
			didReachedEnd = it?.didReachedEnd ?: false
		}

		safeArgs?.also {
			try {
				this.source = ExtensionProvider.forGlobalId(it.sourceGlobalId!!)
			} catch(e: ExtensionNotInstalledException) {
				toast("Source extension isn't installed!")
				finish()
			}
		}

		val filters = safeArgs?.filters

		val columnsCountLand = AtomicInteger(AwerySettings.MEDIA_COLUMNS_COUNT_LAND.value)
		val columnsCountPort = AtomicInteger(AwerySettings.MEDIA_COLUMNS_COUNT_PORT.value)

		val autoColumnsCountLand = columnsCountLand.get() == 0
		val autoColumnsCountPort = columnsCountPort.get() == 0

		if(filters != null) {
			val foundQuery = filters.find { it.key == ExtensionProvider.FILTER_QUERY }
			if(foundQuery != null) queryFilter.setValue(foundQuery.stringValue)
		}

		if(savedInstanceState == null) applyFilters(filters, true)
		else applyFilters(this.filters, false)

		binding = ScreenSearchBinding.inflate(layoutInflater)
		binding.header.edittext.setText(queryFilter.stringValue)
		binding.header.edittext.requestFocus()
		binding.root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))

		binding.header.back.setOnClickListener { finish() }
		binding.header.clear.setOnClickListener { binding.header.edittext.text = null }

		binding.header.filters.setOnClickListener {
			FiltersDialog(filters ?: SettingsList(), source) { updatedFilters ->
				applyFilters(updatedFilters, true)
				didReachedEnd = false
				search(0)
			}.show(this)
		}

		val inputManager = getSystemService(InputMethodManager::class.java)

		binding.header.edittext.setOnEditorActionListener { v, action, _ ->
			if(action != EditorInfo.IME_ACTION_SEARCH) {
				return@setOnEditorActionListener false
			}

			inputManager.hideSoftInputFromWindow(
				binding.header.edittext.windowToken, 0)

			queryFilter.setValue(v.text.toString())
			didReachedEnd = false

			search(0)
			true
		}

		binding.header.root.applyInsets(UI_INSETS, { view, insets ->
			view.topMargin = insets.top
			view.rightMargin = insets.right
			view.leftMargin = insets.left
			true
		})

		binding.swipeRefresher.setOnRefreshListener {
			this.didReachedEnd = false
			search(0)
		}

		binding.swipeRefresher.setColorSchemeColors(
			resolveAttrColor(android.R.attr.colorPrimary))

		binding.swipeRefresher.setProgressBackgroundColorSchemeColor(
			resolveAttrColor(com.google.android.material.R.attr.colorSurface)
		)

		loadingAdapter = SingleViewAdapter.fromBindingDynamic({ parent ->
			val binding = EmptyStateView(parent, false)
			binding.root.useLayoutParams<ViewGroup.LayoutParams> { it.width = MATCH_PARENT }
			binding
		}, LOADING_VIEW_TYPE)

		loadingAdapter!!.isEnabled = false

		val concatAdapter = ConcatAdapter(
			ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), adapter, loadingAdapter)

		val layoutManager = GridLayoutManager(this,
			if(isLandscape) if(autoColumnsCountLand) 3 else columnsCountLand.get()
			else if(autoColumnsCountPort) 5 else columnsCountPort.get())

		binding.recycler.layoutManager = layoutManager
		binding.recycler.adapter = concatAdapter

		binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
				tryLoadMore()
			}
		})

		binding.recycler.applyInsets(UI_INSETS, { view, insets ->
			view.setPadding(
				insets.left + dpPx(8f),
				dpPx(24f),
				insets.right + dpPx(8f),
				dpPx(24f))

			if(isLandscape && autoColumnsCountLand) {
				val freeSpace = (resources.displayMetrics.widthPixels - dpPx(16f) - insets.left - insets.right).toFloat()
				columnsCountLand.set((freeSpace / dpPx(110f)).toInt())
				layoutManager.spanCount = columnsCountLand.get()
			} else if(!isLandscape && autoColumnsCountPort) {
				val freeSpace = (resources.displayMetrics.widthPixels - dpPx(16f) - insets.left - insets.right).toFloat()
				columnsCountPort.set((freeSpace / dpPx(110f)).toInt())
				layoutManager.spanCount = columnsCountPort.get()
			}

			true
		})

		layoutManager.spanSizeLookup = object : SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int {
				if(position < items.size) {
					return 1
				}

				if(isLandscape) {
					return if(columnsCountLand.get() == 0) layoutManager.spanCount
					else columnsCountLand.get()
				}

				return if(columnsCountPort.get() == 0) layoutManager.spanCount
				else columnsCountPort.get()
			}
		}

		if(items.isEmpty()) {
			safeArgs?.preloadedItems?.let { loadedMedia ->
				for(item in loadedMedia) {
					ids[item] = idGenerator.long
				}

				isLoading = false
				items.addAll(loadedMedia)
				adapter.notifyItemRangeInserted(0, items.size)
			}
		} else {
			for(item in items) {
				ids[item] = idGenerator.long
			}

			isLoading = false
		}

		setContentView(binding.root)

		if(Action.SEARCH_BY_TAG == safeArgs?.action) {
			val tag = safeArgs?.queryTag!!.trim { it <= ' ' }
			didReachedEnd = true

			binding.headerWrapper.visibility = View.GONE
			binding.swipeRefresher.isEnabled = false

			loadingAdapter!!.isEnabled = true
			loadingAdapter!!.getBinding { it.startLoading() }

			source!!.filters.addCallback(object : AsyncFuture.Callback<SettingsList?> {
				private fun done() {
					binding.headerWrapper.visibility = View.VISIBLE
					binding.swipeRefresher.isEnabled = true

					didReachedEnd = false
					search(0)
				}

				private fun findTag(items: List<SettingsItem>): SettingsItem? {
					for(item in items) {
						if(item.items != null) {
							val found = findTag(item.items)

							if(found != null) {
								return found
							}
						}

						val title = item.getTitle(this@SearchActivity) ?: continue

						if(Objects.requireNonNull(tag).equals(title.trim { it <= ' ' }, ignoreCase = true)) {
							return item
						}
					}

					return null
				}

				private fun activate(item: SettingsItem, parent: SettingsItem?) {
					if(item.type != null) {
						when(item.type) {
							SettingsItemType.BOOLEAN, SettingsItemType.SCREEN_BOOLEAN -> item.setValue(true)
							SettingsItemType.EXCLUDABLE -> item.setValue(Selection.State.SELECTED)
							else -> {}
						}
					} else if(parent != null) {
						when(parent.type) {
							SettingsItemType.SELECT, SettingsItemType.SELECT_INTEGER -> parent.setValue(item.key)
							SettingsItemType.MULTISELECT -> parent.setValue(setOf(item.key))
							else -> {}
						}
					} else {
						onFailure(UnsupportedOperationException())
					}
				}

				override fun onSuccess(items: SettingsList) {
					for(item in items) {
						item.setAsParentForChildren()
					}

					val found = findTag(items)

					if(found == null) {
						onFailure(UnsupportedOperationException())
					} else {
						var parent: SettingsItem = found
						var root = parent

						while((parent.parent.also { parent = it }) != null) {
							root = parent
						}

						activate(found, found.parent)
						applyFilters(listOf(root), true)
						runOnUiThread({ this.done() }, binding.recycler)
					}
				}

				override fun onFailure(t: Throwable) {
					queryFilter.setValue(tag)

					runOnUiThread({
						binding.header.edittext.setText(tag)
						done()
					}, binding.recycler)
				}
			})
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState.apply {
			putSafeArgs(
				SavedState(
				items = items,
				filters = filters,
				didReachedEnd = didReachedEnd
			)
			)
		})
	}

	private fun applyFilters(newFilters: List<SettingsItem>?, ignoreInternalFilters: Boolean) {
		filters.clear()

		if(newFilters != null) {
			filters.addAll(newFilters)

			val foundQuery = this.filters.find { it.key == ExtensionProvider.FILTER_QUERY }
			val foundPage = this.filters.find { it.key == ExtensionProvider.FILTER_PAGE }

			if(foundQuery != null) {
				if(ignoreInternalFilters) filters.remove(foundQuery)
				else queryFilter = foundQuery
			}

			if(foundPage != null) {
				if(ignoreInternalFilters) filters.remove(foundPage)
				else pageFilter = foundPage
			}
		}

		if(ignoreInternalFilters) {
			filters.add(queryFilter)
			filters.add(pageFilter)
		}
	}

	private fun tryLoadMore() {
		if(!isLoading && !didReachedEnd) {
			val lastIndex = items.size - 1
			val manager = binding.recycler.layoutManager

			if(manager is LinearLayoutManager && manager.findLastVisibleItemPosition() >= lastIndex) {
				pageFilter.setValue(pageFilter.integerValue + 1)
				search(pageFilter.integerValue)
			}
		}
	}

	private fun reachedEnd(wasSearchId: Long) {
		loadingAdapter!!.getBinding { binding ->
			runOnUiThread {
				if(wasSearchId != searchId.toLong()) return@runOnUiThread
				this@SearchActivity.didReachedEnd = true
				binding.title.text = i18n(Res.string.you_reached_end)
				binding.message.text = i18n(Res.string.you_reached_end_description)

				binding.progressBar.visibility = View.GONE
				binding.info.visibility = View.VISIBLE

				isLoading = false
				didReachedEnd = true
			}
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	private fun search(page: Int) {
		if(queryFilter.stringValue == null) {
			queryFilter.setValue("")
		}

		val wasSearchId = ++searchId

		if(page == 0) {
			items.clear()
			adapter.notifyDataSetChanged()
			idGenerator.reset()
		}

		loadingAdapter!!.getBinding { binding ->
			binding.progressBar.visibility = View.VISIBLE
			binding.info.visibility = View.GONE
		}

		loadingAdapter!!.isEnabled = true
		pageFilter.setValue(page)

		source!!.searchMedia(filters).addCallback(object : AsyncFuture.Callback<CatalogSearchResults<out CatalogMedia?>?> {
			override fun onSuccess(items: CatalogSearchResults<out CatalogMedia?>) {
				if(wasSearchId != searchId) return

				if(!items.hasNextPage()) {
					reachedEnd(wasSearchId.toLong())
				}

				MediaUtils.filterMedia(items) { filteredItems ->
					if(filteredItems.isEmpty()) {
						throw ZeroResultsException("No media was found", i18n(Res.string.no_media_found))
					}
					for(item in filteredItems) {
						ids[item] = idGenerator.long
					}
					runOnUiThread {
						if(wasSearchId != searchId) return@runOnUiThread
						this@SearchActivity.isLoading = false

						if(page == 0) {
							this@SearchActivity.items.addAll(filteredItems)
							adapter.notifyDataSetChanged()
						} else {
							val wasSize = this@SearchActivity.items.size
							this@SearchActivity.items.addAll(filteredItems)
							adapter.notifyItemRangeInserted(wasSize, filteredItems.size)
						}
						AweryLifecycle.runDelayed({ tryLoadMore() }, 1000, binding.recycler)
					}
				}

				onFinally()
			}

			override fun onFailure(e: Throwable) {
				if(wasSearchId != searchId) return

				val error = e.explain()
				Log.e(TAG, "Failed to search", e)

				runOnUiThread {
					if(wasSearchId != searchId) return@runOnUiThread
					loadingAdapter!!.getBinding { binding: EmptyStateView ->
						if(wasSearchId != searchId) return@getBinding
						if(page == 0) {
							items.clear()
							adapter.notifyDataSetChanged()
						}

						if(e is ZeroResultsException && page != 0) {
							reachedEnd(wasSearchId.toLong())
						} else {
							binding.title.text = error.title
							binding.message.text = error.message
						}

						binding.progressBar.visibility = View.GONE
						binding.info.visibility = View.VISIBLE
						this@SearchActivity.isLoading = false
					}
				}

				onFinally()
			}

			private fun onFinally() {
				if(wasSearchId != searchId) return
				binding.swipeRefresher.isRefreshing = false
			}
		})
	}

	private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
		init {
			setHasStableIds(true)
		}

		override fun getItemId(position: Int): Long {
			return ids[items[position]] ?: 0
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val binding = GridMediaCatalogBinding.inflate(parent.context.inflater, parent, false)
			val viewHolder = ViewHolder(binding)

			binding.root.useLayoutParams<RecyclerView.LayoutParams> {
				it.width = MATCH_PARENT
				it.rightMargin = dpPx(6f)
				it.leftMargin = dpPx(6f)
			}

			binding.root.setOnClickListener {
				if(Action.PICK_MEDIA == safeArgs?.action) {
					setResult(0, buildIntent {
						putExtra(RESULT_EXTRA_MEDIA, viewHolder.item)
					})

					finish()
					return@setOnClickListener
				}

				startActivity(buildIntent(MediaActivity::class, MediaActivity.Extras(viewHolder.item!!)))
			}

			binding.root.setOnLongClickListener {
				val media: CatalogMedia = viewHolder.item!!
				val index = items.indexOf(media)

				val dialog = MediaActionsDialog(media)

				dialog.updateCallback = {
					MediaUtils.isMediaFiltered(media) { isFiltered: Boolean? ->
						if(!isFiltered!!) return@isMediaFiltered
						runOnUiThread {
							items.remove(media)
							notifyItemRemoved(index)
						}
					}
				}

				dialog.show(parent.context)
				true
			}

			return viewHolder
		}

		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			holder.bind(items[position])
		}

		override fun getItemCount(): Int {
			return items.size
		}
	}

	private class ViewHolder(private val binding: GridMediaCatalogBinding) : RecyclerView.ViewHolder(binding.root) {
		var item: CatalogMedia? = null
			private set

		fun bind(item: CatalogMedia) {
			this.item = item

			binding.title.text = item.title
			binding.ongoing.visibility = if(item.status == CatalogMedia.Status.ONGOING) View.VISIBLE else View.GONE

			if(item.averageScore != null) {
				binding.scoreWrapper.visibility = View.VISIBLE
				binding.score.text = item.averageScore.toString()
			} else {
				binding.scoreWrapper.visibility = View.GONE
			}

			try {
				Glide.with(binding.root)
					.load(item.poster)
					.transition(DrawableTransitionOptions.withCrossFade())
					.into(binding.mediaItemBanner)
			} catch(e: IllegalArgumentException) {
				Log.e(TAG, "Failed to load a poster", e)
			}
		}
	}

	companion object {
		const val RESULT_EXTRA_MEDIA: String = "media"
		private const val LOADING_VIEW_TYPE = 1
		private const val TAG = "SearchActivity"
	}
}