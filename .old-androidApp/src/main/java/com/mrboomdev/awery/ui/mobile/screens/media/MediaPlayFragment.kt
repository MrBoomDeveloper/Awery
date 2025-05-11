package com.mrboomdev.awery.ui.mobile.screens.media

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
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
import com.mrboomdev.awery.app.App.Companion.openUrl
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runOnUiThread
import com.mrboomdev.awery.data.Constants
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.data.settings.SettingsItemType
import com.mrboomdev.awery.data.settings.SettingsList
import com.mrboomdev.awery.databinding.ItemListDropdownBinding
import com.mrboomdev.awery.databinding.LayoutWatchVariantsBinding
import com.mrboomdev.awery.extensions.Extension
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.extensions.ExtensionsFactory
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress
import com.mrboomdev.awery.extensions.data.CatalogSearchResults
import com.mrboomdev.awery.extensions.data.CatalogVideo
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.Platform.toast
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.mobile.screens.player.PlayerActivity
import com.mrboomdev.awery.ui.mobile.screens.search.SearchActivity
import com.mrboomdev.awery.ui.mobile.screens.media.MediaPlayEpisodesAdapter.OnEpisodeSelectedListener
import com.mrboomdev.awery.util.adapters.MediaAdapter
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException
import com.mrboomdev.awery.util.exceptions.OkiThrowableMessage
import com.mrboomdev.awery.util.exceptions.isNetworkException
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.bottomPadding
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.get
import com.mrboomdev.awery.util.extensions.leftPadding
import com.mrboomdev.awery.util.extensions.rightPadding
import com.mrboomdev.awery.util.extensions.setImageTintColor
import com.mrboomdev.awery.util.extensions.setVerticalPadding
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.topPadding
import com.mrboomdev.awery.ui.mobile.components.EmptyStateView
import com.mrboomdev.awery.utils.exceptions.BotSecurityBypassException
import com.mrboomdev.awery.util.ui.adapter.DropdownAdapter
import com.mrboomdev.awery.util.ui.adapter.DropdownBindingAdapter
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter.BindingSingleViewAdapter
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.startActivityForResult
import com.mrboomdev.safeargsnext.owner.SafeArgsFragment
import com.mrboomdev.safeargsnext.util.rememberSafeArgs
import com.squareup.moshi.adapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class MediaPlayFragment: Fragment(), SafeArgsFragment<MediaPlayFragment.Args>, OnEpisodeSelectedListener {
	private val sourceStatuses: MutableMap<ExtensionProvider, ExtensionStatus> = HashMap()
	private var placeholderAdapter: BindingSingleViewAdapter<EmptyStateView>? = null
	private var variantsAdapter: BindingSingleViewAdapter<LayoutWatchVariantsBinding>? = null
	private var sourcesDropdownAdapter: DropdownAdapter<ExtensionProvider>? = null
	private var concatAdapter: ConcatAdapter? = null
	private var templateEpisodes: List<CatalogVideo>? = null
	private var providers: MutableList<ExtensionProvider>? = null
	private var episodesAdapter: MediaPlayEpisodesAdapter? = null
	private var recycler: RecyclerView? = null
	private var selectedSource: ExtensionProvider? = null
	private var viewMode: AwerySettings.EpisodesDisplayModeValue? = null
	private var searchId: String? = null
	private var searchTitle: String? = null
	private var autoChangeSource = true
	private var autoChangeTitle = true
	private var changeSettings = true
	private var currentSourceIndex = 0
	private var loadId = 0L
	private var media: CatalogMedia? = null

	private val queryFilter = SettingsItem(SettingsItemType.STRING, ExtensionProvider.FILTER_QUERY)
	private val filters = SettingsList(queryFilter, SettingsItem(SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0))

	class Args(val media: CatalogMedia)

	enum class ViewMode {
		GRID, LIST
	}

	override fun onEpisodeSelected(episode: CatalogVideo, episodes: List<CatalogVideo>) {
		startActivity(PlayerActivity::class, PlayerActivity.Extras(
			episode = episode,
			episodes = episodes,
			source = selectedSource!!.globalId
		))

		lifecycleScope.launch(Dispatchers.IO) {
			val dao = database.mediaProgressDao
			var progress = dao[media!!.globalId]
			if(progress == null) progress = CatalogMediaProgress(media!!.globalId)

			progress.lastWatchSource = selectedSource!!.id

			val foundMedia = episodesAdapter!!.media
			progress.lastId = foundMedia.mediaId
			progress.lastTitle = foundMedia.title
			dao.insert(progress)
		}
	}

	private enum class ExtensionStatus {
		OK, OFFLINE, SERVER_DOWN, BROKEN_PARSER, NOT_FOUND, NONE
	}

	override fun onResume() {
		super.onResume()
		if(Constants.alwaysTrue()) return

		if(changeSettings) {
			val viewMode = AwerySettings.EPISODES_DISPLAY_MODE.value

			if(viewMode != this.viewMode) {
				this.viewMode = viewMode

				recycler!!.layoutManager = when(viewMode) {
					AwerySettings.EpisodesDisplayModeValue.LIST -> LinearLayoutManager(requireContext())
					AwerySettings.EpisodesDisplayModeValue.GRID -> {
						val columnsCount = AtomicInteger(3)
						val layoutManager = GridLayoutManager(requireContext(), columnsCount.get())

						recycler!!.applyInsets(UI_INSETS, { view, insets ->
							view.setVerticalPadding(dpPx(24f))
							view.leftPadding = insets.left + dpPx(8f)
							view.rightPadding = insets.right + dpPx(8f)

							val columnSize = dpPx(80f)
							val freeSpace = (requireContext().resources.displayMetrics.widthPixels - dpPx(16f) - insets.left - insets.right).toFloat()
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

	@OptIn(ExperimentalStdlibApi::class)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(view.context) {
		if(media == null) {
			media = rememberSafeArgs!!.media
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

		val more = i18n(Res.string.manual_search)
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

					requireActivity().startActivityForResult(buildIntent(SearchActivity::class, SearchActivity.Extras(
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

						placeholderAdapter!!.getBinding { placeholder ->
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
			handleExceptionUi(ZeroResultsException("No extensions was found", i18n(Res.string.no_extensions_found)))
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

	@OptIn(ExperimentalStdlibApi::class)
	private fun loadEpisodesFromSource(source: ExtensionProvider, media: CatalogMedia) {
		val myId = ++loadId

		variantsAdapter!!.getBinding { binding ->
			runOnUiThread {
				binding.searchStatus.text = i18n(Res.string.searching_episodes_for, media.title)
				binding.searchStatus.setOnClickListener {
					startActivity(MediaActivity::class, MediaActivity.Extras(media)) }
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
						binding.searchStatus.text = i18n(Res.string.selected_s, media.title)
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
					handleExceptionUi(e, source, media)
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

		var lastUsedTitleIndex = 0

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

					if(autoChangeTitle && media!!.titles != null && lastUsedTitleIndex < media!!.titles!!.size - 1) {
						val newIndex = ++lastUsedTitleIndex
						queryFilter.setValue(media!!.titles!![newIndex])
						source.searchMedia(filters).addCallback(this)

						variantsAdapter!!.getBinding { binding ->
							runOnUiThread {
								binding.searchStatus.text = i18n(Res.string.searching_for, queryFilter.stringValue)
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
						handleExceptionUi(e, source)
					}
				}
			}
		}

		context ?: return

		if(searchId != null) {
			variantsAdapter!!.getBinding { binding ->
				runOnUiThread {
					binding.searchStatus.text = i18n(Res.string.searching_for, searchTitle)
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
							binding.searchStatus.text = i18n(Res.string.searching_for, queryFilter.stringValue)
							binding.searchStatus.setOnClickListener(null)
						}
					}

					source.searchMedia(filters).addCallback(foundMediaCallback)
				}
			})
		} else {
			variantsAdapter!!.getBinding { binding ->
				runOnUiThread {
					binding.searchStatus.text = i18n(Res.string.searching_for, queryFilter.stringValue)
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

	private fun handleExceptionMark(source: ExtensionProvider, t: Throwable) {
		if(source !== selectedSource) return

		/*sourceStatuses.put(source, switch(ExceptionDescriptor.getReason(throwable)) {
			case SERVER_DOWN -> ExtensionStatus.SERVER_DOWN;
			case OTHER, UNIMPLEMENTED -> ExtensionStatus.BROKEN_PARSER;
		});*/

		if(t is ZeroResultsException) sourceStatuses[source] = ExtensionStatus.NOT_FOUND
		else if(t.isNetworkException) sourceStatuses[source] = ExtensionStatus.OFFLINE
		else sourceStatuses[source] = ExtensionStatus.BROKEN_PARSER
	}

	private fun handleExceptionUi(
		throwable: Throwable,
		source: ExtensionProvider? = null,
		media: CatalogMedia? = null
	) {
		if(source !== selectedSource && source != null) return
		val error = OkiThrowableMessage(throwable)

		context ?: return

		variantsAdapter!!.getBinding { binding ->
			binding.searchStatus.text = error.title
			binding.searchStatus.setOnClickListener(null)
		}

		placeholderAdapter!!.getBinding { binding ->
			runOnUiThread {
				if(error.unwrapped is BotSecurityBypassException && source != null && (media?.url != null || source.previewUrl != null)) {
					binding.setInfo(
						title = error.title,
						message = error.message,
						buttonText = "Retry",
						buttonClickListener = { loadEpisodesFromSource(source) },
						button2OnClick = { openUrl(requireContext(), media?.url ?: source.previewUrl!!, true) },
						button2Text = "Open website"
					)
				} else {
					binding.setInfo(
						title = error.title,
						message = error.message
					)
				}

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
			EmptyStateView(parent, false)
		}, VIEW_TYPE_ERROR)

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