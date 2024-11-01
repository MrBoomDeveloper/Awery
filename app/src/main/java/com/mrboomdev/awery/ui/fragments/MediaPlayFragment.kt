@file:OptIn(ExperimentalStdlibApi::class)

package com.mrboomdev.awery.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.core.graphics.Insets
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.elevation.SurfaceColors
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.app.AweryLifecycle.Companion.startActivityForResult
import com.mrboomdev.awery.app.data.Constants
import com.mrboomdev.awery.app.data.settings.NicePreferences
import com.mrboomdev.awery.app.data.settings.SettingsItem
import com.mrboomdev.awery.app.data.settings.SettingsItemType
import com.mrboomdev.awery.app.data.settings.SettingsList
import com.mrboomdev.awery.databinding.ItemListDropdownBinding
import com.mrboomdev.awery.databinding.LayoutWatchVariantsBinding
import com.mrboomdev.awery.extensions.Extension
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.extensions.ExtensionsFactory
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress
import com.mrboomdev.awery.extensions.data.CatalogSearchResults
import com.mrboomdev.awery.extensions.data.CatalogVideo
import com.mrboomdev.awery.ui.activity.MediaActivity
import com.mrboomdev.awery.ui.activity.player.PlayerActivity
import com.mrboomdev.awery.ui.activity.search.SearchActivity
import com.mrboomdev.awery.ui.adapter.MediaPlayEpisodesAdapter
import com.mrboomdev.awery.ui.adapter.MediaPlayEpisodesAdapter.OnEpisodeSelectedListener
import com.mrboomdev.awery.util.MediaUtils
import com.mrboomdev.awery.util.NiceUtils
import com.mrboomdev.awery.util.adapters.MediaAdapter
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.bottomPadding
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.get
import com.mrboomdev.awery.util.extensions.leftPadding
import com.mrboomdev.awery.util.extensions.rightPadding
import com.mrboomdev.awery.util.extensions.screenWidth
import com.mrboomdev.awery.util.extensions.setImageTintColor
import com.mrboomdev.awery.util.extensions.setVerticalPadding
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.topPadding
import com.mrboomdev.awery.util.ui.EmptyView
import com.mrboomdev.awery.util.ui.ViewUtil
import com.mrboomdev.awery.util.ui.adapter.DropdownAdapter
import com.mrboomdev.awery.util.ui.adapter.DropdownBindingAdapter
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter.BindingSingleViewAdapter
import com.mrboomdev.safeargsnext.SafeArgsIntent
import com.squareup.moshi.adapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

class MediaPlayFragment @JvmOverloads constructor(
	private var media: CatalogMedia? = null
) : Fragment(), OnEpisodeSelectedListener {
	private val sourceStatuses: MutableMap<ExtensionProvider, ExtensionStatus> = HashMap()
	private val queryFilter =
		SettingsItem(SettingsItemType.STRING, ExtensionProvider.FILTER_QUERY)
	private val filters = SettingsList(
		queryFilter,
		SettingsItem(SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0)
	)
	private var placeholderAdapter: BindingSingleViewAdapter<EmptyView>? = null
	private var variantsAdapter: BindingSingleViewAdapter<LayoutWatchVariantsBinding>? = null
	private var sourcesDropdownAdapter: DropdownAdapter<ExtensionProvider>? = null
	private var concatAdapter: ConcatAdapter? = null
	private var templateEpisodes: List<CatalogVideo>? = null
	private var providers: MutableList<ExtensionProvider>? = null
	private var episodesAdapter: MediaPlayEpisodesAdapter? = null
	private var recycler: RecyclerView? = null
	private var selectedSource: ExtensionProvider? = null
	private var viewMode: ViewMode? = null
	private var searchId: String? = null
	private var searchTitle: String? = null
	private var autoChangeSource = true
	private var autoChangeTitle = true
	private var changeSettings = true
	private var currentSourceIndex = 0
	private var loadId: Long = 0

	enum class ViewMode {
		GRID, LIST
	}

	override fun onEpisodeSelected(episode: CatalogVideo, episodes: List<CatalogVideo>) {
		PlayerActivity.selectSource(selectedSource)

		val intent = Intent(requireContext(), PlayerActivity::class.java)
		intent.putExtra("episode", episode)
		intent.putExtra("episodes", episodes as Serializable)
		startActivity(intent)

		lifecycleScope.launch(Dispatchers.IO) {
			val dao = database.mediaProgressDao
			var progress = dao[media!!.globalId]
			if(progress == null) progress = CatalogMediaProgress(media!!.globalId)

			progress.lastWatchSource = selectedSource!!.id

			val foundMedia = episodesAdapter!!.media
			progress.lastId = foundMedia.id
			progress.lastTitle = foundMedia.title
			dao.insert(progress)
		}
	}

	private enum class ExtensionStatus {
		OK, OFFLINE, SERVER_DOWN, BROKEN_PARSER, NOT_FOUND, NONE;

		val isUnknown: Boolean
			get() = this == NONE
	}

	/**
	 * DO NOT CALL THIS CONSTRUCTOR!
	 * @author MrBoomDev
	 */
	init {
		val bundle = Bundle()
		bundle.putSerializable("media", media)
		arguments = bundle
	}

	override fun onResume() {
		super.onResume()
		if(Constants.alwaysTrue()) return

		if(changeSettings) {
			val prefs = NicePreferences.getPrefs()
			val viewMode = NiceUtils.parseEnum(prefs.getString("settings_ui_episodes_mode"), ViewMode.LIST)!!

			if(viewMode != this.viewMode) {
				this.viewMode = viewMode

				recycler!!.layoutManager = when(viewMode) {
					ViewMode.LIST -> LinearLayoutManager(requireContext())
					ViewMode.GRID -> {
						val columnsCount = AtomicInteger(3)
						val layoutManager = GridLayoutManager(requireContext(), columnsCount.get())

						recycler!!.applyInsets(UI_INSETS, { view, insets ->
							view.setVerticalPadding(dpPx(24f))
							view.leftPadding = insets.left + dpPx(8f)
							view.rightPadding = insets.right + dpPx(8f)

							val columnSize = dpPx(80f)
							val freeSpace = (requireContext().screenWidth - dpPx(16f) - insets.left - insets.right).toFloat()
							columnsCount.set((freeSpace / columnSize).toInt())
							layoutManager.spanCount = columnsCount.get()
							true
						})

						layoutManager.spanSizeLookup = object : SpanSizeLookup() {
							override fun getSpanSize(position: Int): Int {
								/* Don't ask. I don't know how it is working, so please don't ask about it. */
								return if((concatAdapter!!.getItemViewType(position) == VIEW_TYPE_EPISODE)) 1 else columnsCount.get()
							}
						}

						layoutManager
					}
				}
			}
		}

		changeSettings = false
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		if(media == null) {
			media = NiceUtils.requireArgument(this, "media")
		}

		val type = media!!.type ?: CatalogMedia.Type.TV

		providers = ArrayList(ExtensionsFactory.getExtensions__Deprecated(Extension.FLAG_WORKING)
			.map { extension ->
				extension.getProviders(
					when(type) {
						CatalogMedia.Type.TV, CatalogMedia.Type.MOVIE -> ExtensionProvider.FEATURE_MEDIA_WATCH
						CatalogMedia.Type.BOOK, CatalogMedia.Type.POST -> ExtensionProvider.FEATURE_MEDIA_READ
					}
				)
			}.flatten().sorted().toList())

		sourcesDropdownAdapter = object : DropdownBindingAdapter<ExtensionProvider, ItemListDropdownBinding>(providers) {
			override fun onCreateBinding(parent: ViewGroup, viewType: Int): ItemListDropdownBinding {
				return ItemListDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			}

			override fun getView(binding: ItemListDropdownBinding): View {
				return binding.root
			}

			override fun onBindItem(item: ExtensionProvider, binding: ItemListDropdownBinding) {
				binding.root.setBackgroundColor(
					if(item !== selectedSource) 0
					else SurfaceColors.SURFACE_3.getColor(requireContext())
				)

				binding.title.text = item.name
				val status = sourceStatuses[item]

				if(status != null) {
					val statusColor = if(status == ExtensionStatus.OK) Color.GREEN else Color.RED

					val iconRes = when(status) {
						ExtensionStatus.OK -> R.drawable.ic_check
						ExtensionStatus.BROKEN_PARSER -> R.drawable.ic_round_error_24
						ExtensionStatus.SERVER_DOWN -> R.drawable.ic_round_block_24
						ExtensionStatus.OFFLINE -> R.drawable.ic_round_signal_no_internet_24
						ExtensionStatus.NOT_FOUND -> R.drawable.ic_zero
						ExtensionStatus.NONE -> null
					}

					if(iconRes != null) {
						binding.icon.setImageResource(iconRes)
						binding.icon.visibility = View.VISIBLE
						binding.icon.setImageTintColor(statusColor)
					} else {
						binding.icon.visibility = View.GONE
					}
				} else {
					binding.icon.visibility = View.GONE
				}
			}
		}

		val more = getString(R.string.manual_search)
		val titles = media!!.titles!!.toMutableList()
		titles.add(more)

		val titlesAdapter = object : DropdownBindingAdapter<String, ItemListDropdownBinding>(titles) {
			override fun onCreateBinding(parent: ViewGroup, viewType: Int): ItemListDropdownBinding {
				return ItemListDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			}

			override fun getView(binding: ItemListDropdownBinding): View {
				return binding.root
			}

			override fun onBindItem(item: String, binding: ItemListDropdownBinding) {
				binding.title.text = item
			}
		}

		variantsAdapter!!.getBinding { binding ->
			binding.sourceDropdown.setAdapter(sourcesDropdownAdapter)
			binding.searchDropdown.setAdapter(titlesAdapter)

			binding.sourceDropdown.onItemClickListener = OnItemClickListener { _, _, position, _ ->
				searchId = null
				searchTitle = null

				episodesAdapter!!.setItems(null, null)
				autoChangeSource = false
				autoChangeTitle = true
				selectProvider(providers!![position])
			}

			binding.searchDropdown.onItemClickListener = OnItemClickListener itemListener@{ _, _, position, _ ->
				val title = titles[position]

				if(selectedSource == null) {
					if(title == more) {
						binding.searchDropdown.setText(queryFilter.stringValue, false)
					}

					toast("You haven't selected any source!", 1)
					return@itemListener
				}

				if(title == more) {
					binding.searchDropdown.setText(queryFilter.stringValue, false)

					startActivityForResult(requireActivity(), SafeArgsIntent(
						requireContext(), SearchActivity::class, SearchActivity.Extras(
							action = SearchActivity.Action.PICK_MEDIA,
							sourceGlobalId = selectedSource!!.globalId,
							filters = filters
						)), { _, result ->
						if(result == null) return@startActivityForResult
						val media = result.get<CatalogMedia>(SearchActivity.RESULT_EXTRA_MEDIA) ?: return@startActivityForResult

						searchId = null
						searchTitle = null

						binding.searchDropdown.setText(media.title, false)
						queryFilter.setValue(media.title)

						placeholderAdapter!!.getBinding { placeholder: EmptyView ->
							placeholder.startLoading()
							placeholderAdapter!!.isEnabled = true
							episodesAdapter!!.setItems(media, emptyList())

							autoChangeSource = false
							autoChangeTitle = false

							episodesAdapter!!.setItems(null, null)
							loadEpisodesFromSource(selectedSource!!, media)
						}
					})
				} else {
					searchId = null
					searchTitle = null

					episodesAdapter!!.setItems(null, null)
					autoChangeSource = false
					autoChangeTitle = false
					queryFilter.setValue(title)
					selectProvider(selectedSource!!)
				}
			}
		}

		queryFilter.setValue(media!!.title)

		if(providers?.isEmpty() == true) {
			handleExceptionUi(null, ZeroResultsException("No extensions was found", R.string.no_extensions_found))
			variantsAdapter!!.isEnabled = false
			return
		}

		lifecycleScope.launch(Dispatchers.IO) {
			val progress = database.mediaProgressDao[media!!.globalId]

			val mediaSource = try {
				ExtensionProvider.forGlobalId(media!!.globalId)
			} catch(e: ExtensionNotInstalledException) {
				Log.e(TAG, "Source extension isn't installed!", e)
				null
			}

			if(progress != null) {
				searchId = progress.lastId
				searchTitle = progress.lastTitle

				providers!!.sortWith { a, b ->
					if(a.id == progress.lastWatchSource) return@sortWith -1
					if(b.id == progress.lastWatchSource) return@sortWith 1

					if(mediaSource != null) {
						if(a.id == mediaSource.id) return@sortWith -1
						if(b.id == mediaSource.id) return@sortWith 1
					}

					0
				}

				sourcesDropdownAdapter!!.setItems(providers)
			} else if(mediaSource != null) {
				providers!!.sortWith { a, b ->
					if(a.id == mediaSource.id) return@sortWith -1
					if(b.id == mediaSource.id) return@sortWith 1
					0
				}

				sourcesDropdownAdapter!!.setItems(providers)
			}
			if(mediaSource != null) {
				mediaSource.getVideos(
					SettingsList(
						SettingsItem(SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0),
						SettingsItem(
							SettingsItemType.JSON, ExtensionProvider.FILTER_MEDIA,
							getMoshi(MediaAdapter).adapter<CatalogMedia>().toJson(media)
						)
					)
				).addCallback(object : AsyncFuture.Callback<List<CatalogVideo>> {
					override fun onSuccess(catalogEpisodes: List<CatalogVideo>) {
						templateEpisodes = catalogEpisodes
						currentSourceIndex = 0
						runOnUiThread { selectProvider(providers!![0]) }
					}

					override fun onFailure(e: Throwable) {
						// Don't merge any data. Just load original data
						currentSourceIndex = 0
						runOnUiThread { selectProvider(providers!![0]) }
					}
				})
			} else {
				currentSourceIndex = 0
				runOnUiThread { selectProvider(providers!![0]) }
			}
		}
	}

	private fun selectProvider(provider: ExtensionProvider) {
		variantsAdapter!!.getBinding { binding -> binding.sourceDropdown.setText(provider.name, false) }

		loadEpisodesFromSource(provider)
		variantsAdapter!!.getBinding { binding -> binding.variantWrapper.visibility = View.GONE }
	}

	private fun loadEpisodesFromSource(source: ExtensionProvider, media: CatalogMedia) {
		val myId = ++loadId

		variantsAdapter!!.getBinding { binding ->
			runOnUiThread {
				binding.searchStatus.text = "Searching episodes for \"" + media.title + "\"..."
				binding.searchStatus.setOnClickListener { startActivity(
					MediaActivity::class, MediaActivity.Extras(media)) }
			}
		}

		source.getVideos(
			SettingsList(
				SettingsItem(SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0),
				SettingsItem(
					SettingsItemType.JSON, ExtensionProvider.FILTER_MEDIA,
					getMoshi(MediaAdapter).adapter<CatalogMedia>().toJson(media)
				)
			)
		).addCallback(object : AsyncFuture.Callback<List<CatalogVideo?>?> {
			override fun onSuccess(result: List<CatalogVideo?>) {
				if(source !== selectedSource || myId != loadId) return
				sourceStatuses[source] = ExtensionStatus.OK

				val episodes = ArrayList(result)
				episodes.sortWith(Comparator.comparing { it!!.number })

				if(templateEpisodes != null) {
					for(episode in episodes) {
						val templateEpisode = templateEpisodes!!
							.filter { it.number == episode?.number }
							.getOrNull(0)

						if(templateEpisode == null) {
							continue
						}

						if(episode?.banner == null) {
							episode?.banner = templateEpisode.banner
						}
					}
				}

				runOnUiThread {
					variantsAdapter!!.getBinding { binding ->
						binding.searchStatus.text = "Selected \"${media.title}\""
						startActivity(MediaActivity::class, args = MediaActivity.Extras(media))
					}

					placeholderAdapter!!.isEnabled = false
					episodesAdapter!!.setItems(media, episodes)
				}
			}

			override fun onFailure(e: Throwable) {
				if(source !== selectedSource || myId != loadId) return

				Log.e(TAG, "Failed to load episodes!", e)

				runOnUiThread {
					handleExceptionMark(source, e)
					if(autoSelectNextSource()) return@runOnUiThread
					handleExceptionUi(source, e)
				}
			}
		})
	}

	private fun loadEpisodesFromSource(source: ExtensionProvider) {
		this.selectedSource = source
		val myId = ++loadId

		placeholderAdapter!!.isEnabled = true
		placeholderAdapter!!.getBinding { it.startLoading() }

		runOnUiThread {
			try {
				episodesAdapter!!.setItems(media, emptyList())
			} catch(e: IllegalStateException) {
				Log.e(TAG, "Lets hope that the episodes adapter was just created ._.")
			}
		}

		val lastUsedTitleIndex = AtomicInteger(0)

		if(autoChangeSource) {
			queryFilter.setValue(media!!.title)
		}

		variantsAdapter!!.getBinding { binding -> binding.searchDropdown.setText(queryFilter.stringValue, false) }

		val foundMediaCallback = object : AsyncFuture.Callback<CatalogSearchResults<out CatalogMedia?>?> {
			override fun onSuccess(media: CatalogSearchResults<out CatalogMedia>) {
				if(source !== selectedSource || myId != loadId) return
				loadEpisodesFromSource(source, media[0])
			}

			override fun onFailure(e: Throwable) {
				if(myId != loadId) return

				Log.e(TAG, "Failed to search media!", e)

				runOnUiThread {
					context ?: return@runOnUiThread

					if(autoChangeTitle && media!!.titles != null && lastUsedTitleIndex.get() < media!!.titles!!.size - 1) {
						val newIndex = lastUsedTitleIndex.incrementAndGet()
						queryFilter.setValue(media!!.titles!![newIndex])
						source.searchMedia(filters).addCallback(this)

						variantsAdapter!!.getBinding { binding ->
							runOnUiThread {
								binding.searchStatus.text = "Searching for \"" + queryFilter.stringValue + "\"..."
								binding.searchStatus.setOnClickListener(null)
							}
						}

						variantsAdapter!!.getBinding { binding ->
							binding.searchDropdown.setText(
								queryFilter.stringValue, false
							)
						}
					} else {
						handleExceptionMark(source, e)
						if(autoSelectNextSource()) return@runOnUiThread
						handleExceptionUi(source, e)
					}
				}
			}
		}

		context ?: return

		if(searchId != null) {
			variantsAdapter!!.getBinding { binding ->
				runOnUiThread {
					binding.searchStatus.text = "Searching for \"$searchTitle\"..."
					binding.searchStatus.setOnClickListener(null)
				}
			}

			source.getMedia(searchId).addCallback(object : AsyncFuture.Callback<CatalogMedia?> {
				override fun onSuccess(media: CatalogMedia) {
					if(source !== selectedSource || myId != loadId) return
					loadEpisodesFromSource(source, media)
					searchId = null
				}

				override fun onFailure(e: Throwable) {
					if(source !== selectedSource || myId != loadId) return

					variantsAdapter!!.getBinding { binding ->
						runOnUiThread {
							binding.searchStatus.text = "Searching for \"${queryFilter.stringValue}\"..."
							binding.searchStatus.setOnClickListener(null)
						}
					}

					source.searchMedia(filters).addCallback(foundMediaCallback)
				}
			})
		} else {
			variantsAdapter!!.getBinding { binding ->
				runOnUiThread {
					binding.searchStatus.text = "Searching for \"" + queryFilter.stringValue + "\"..."
					binding.searchStatus.setOnClickListener(null)
				}
			}

			source.searchMedia(filters).addCallback(foundMediaCallback)
		}
	}

	private fun autoSelectNextSource(): Boolean {
		if(!autoChangeSource) return false

		currentSourceIndex++

		if(currentSourceIndex >= providers!!.size) {
			return false
		}

		selectProvider(providers!![currentSourceIndex])
		return true
	}

	private fun handleExceptionMark(source: ExtensionProvider, throwable: Throwable) {
		if(source !== selectedSource) return
		val error = ExceptionDescriptor(throwable)

		/*sourceStatuses.put(source, switch(ExceptionDescriptor.getReason(throwable)) {
			case SERVER_DOWN -> ExtensionStatus.SERVER_DOWN;
			case OTHER, UNIMPLEMENTED -> ExtensionStatus.BROKEN_PARSER;
		});*/
		if(throwable is ZeroResultsException) sourceStatuses[source] = ExtensionStatus.NOT_FOUND
		else if(error.isNetworkException) sourceStatuses[source] = ExtensionStatus.OFFLINE
		else sourceStatuses[source] = ExtensionStatus.BROKEN_PARSER
	}

	private fun handleExceptionUi(source: ExtensionProvider?, throwable: Throwable) {
		if(source !== selectedSource && source != null) return
		val error = ExceptionDescriptor(throwable)

		val context = context ?: return

		variantsAdapter!!.getBinding { binding ->
			binding.searchStatus.text = error.getMessage(context)
			binding.searchStatus.setOnClickListener(null)
		}

		placeholderAdapter!!.getBinding { binding ->
			runOnUiThread {
				binding.setInfo(error.getTitle(context), error.getMessage(context))
				placeholderAdapter!!.setEnabled(true)
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		variantsAdapter = SingleViewAdapter.fromBindingDynamic({ parent ->
			LayoutWatchVariantsBinding.inflate(inflater, parent, false) }, VIEW_TYPE_VARIANTS)

		placeholderAdapter = SingleViewAdapter.fromBindingDynamic({ parent ->
			EmptyView(parent, false) }, VIEW_TYPE_ERROR)

		episodesAdapter = MediaPlayEpisodesAdapter()
		episodesAdapter!!.setOnEpisodeSelectedListener(this)

		return RecyclerView(inflater.context).apply {
			layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
			bottomPadding = dpPx(12f)
			clipToPadding = false

			adapter = ConcatAdapter(ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.setIsolateViewTypes(false)
				.build(), variantsAdapter, episodesAdapter, placeholderAdapter)

			applyInsets(UI_INSETS, { _, insets ->
				topPadding = insets.top
				rightPadding = insets.right
				true
			}, container)
		}
	}

	companion object {
		const val VIEW_TYPE_VARIANTS = 1
		const val VIEW_TYPE_ERROR = 2
		const val VIEW_TYPE_EPISODE = 3
		private const val TAG = "MediaPlayFragment"
	}
}