package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.app.data.Constants.alwaysTrue;
import static com.mrboomdev.awery.app.data.db.AweryDB.getDatabase;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import static java.util.Objects.requireNonNull;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.Lifecycle;
import com.mrboomdev.awery.app.data.settings.NicePreferences;
import com.mrboomdev.awery.app.data.settings.base.SettingsItem;
import com.mrboomdev.awery.app.data.settings.base.SettingsItemType;
import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.databinding.ItemListDropdownBinding;
import com.mrboomdev.awery.databinding.LayoutWatchVariantsBinding;
import com.mrboomdev.awery.ext.data.Media;
import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.__ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.ui.activity.player.PlayerActivity;
import com.mrboomdev.awery.ui.activity.search.SearchActivity;
import com.mrboomdev.awery.ui.adapter.MediaPlayEpisodesAdapter;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ExtensionComponentMissingException;
import com.mrboomdev.awery.util.exceptions.ExtensionNotInstalledException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.ArrayListAdapter;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaPlayFragment extends Fragment implements MediaPlayEpisodesAdapter.OnEpisodeSelectedListener {
	private static final int REQUEST_CODE_PICK_MEDIA = Lifecycle.getActivityResultCode();
	public static final int VIEW_TYPE_VARIANTS = 1;
	public static final int VIEW_TYPE_ERROR = 2;
	public static final int VIEW_TYPE_EPISODE = 3;
	private static final String TAG = "MediaPlayFragment";
	private final Map<__ExtensionProvider, ExtensionStatus> sourceStatuses = new HashMap<>();
	private final SettingsItem queryFilter = new SettingsItem(SettingsItemType.STRING, ExtensionConstants.FILTER_QUERY);
	private final SettingsList filters = new SettingsList(queryFilter,
			new SettingsItem(SettingsItemType.INTEGER, ExtensionConstants.FILTER_PAGE, 0));
	private SingleViewAdapter.BindingSingleViewAdapter<EmptyView> placeholderAdapter;
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutWatchVariantsBinding> variantsAdapter;
	private ArrayListAdapter<__ExtensionProvider> sourcesDropdownAdapter;
	private ConcatAdapter concatAdapter;
	private List<? extends CatalogVideo> templateEpisodes;
	private List<__ExtensionProvider> providers;
	private MediaPlayEpisodesAdapter episodesAdapter;
	private RecyclerView recycler;
	private __ExtensionProvider selectedSource;
	private ViewMode viewMode;
	private Media media;
	private String searchId, searchTitle;
	private boolean autoChangeSource = true, autoChangeTitle = true, changeSettings = true;
	private int currentSourceIndex = 0;
	private long loadId;

	public enum ViewMode { GRID, LIST }

	@Override
	public void onEpisodeSelected(@NonNull CatalogVideo episode, @NonNull List<CatalogVideo> episodes) {
		PlayerActivity.selectSource(selectedSource);

		var intent = new Intent(requireContext(), PlayerActivity.class);
		intent.putExtra(PlayerActivity.ARGUMENT_EPISODE, episode);
		intent.putExtra(PlayerActivity.ARGUMENT_EPISODES, (Serializable) episodes);
		startActivity(intent);

		thread(() -> {
			var dao = getDatabase().getMediaProgressDao();

			var progress = dao.get(media.getGlobalId());
			if(progress == null) progress = new CatalogMediaProgress(media.getGlobalId());

			progress.lastWatchSource = selectedSource.getId();

			var foundMedia = episodesAdapter.getMedia();
			progress.lastId = foundMedia.getGlobalId();
			progress.lastTitle = foundMedia.getTitle();

			dao.insert(progress);
		});
	}

	private enum ExtensionStatus {
		OK, OFFLINE, SERVER_DOWN, BROKEN_PARSER, NOT_FOUND, NONE;

		public boolean isGood() {
			return this == OK;
		}

		public boolean isBad() {
			return this == OFFLINE || this == SERVER_DOWN || this == BROKEN_PARSER || this == NOT_FOUND;
		}

		public boolean isUnknown() {
			return this == NONE;
		}
	}

	public MediaPlayFragment(Media media) {
		this.media = media;

		var bundle = new Bundle();
		bundle.putSerializable("media", media);
		setArguments(bundle);
	}

	/**
	 * DO NOT CALL THIS CONSTRUCTOR!
	 * @author MrBoomDev
	 */
	public MediaPlayFragment() {
		this(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(alwaysTrue()) return;

		if(changeSettings) {
			var prefs = NicePreferences.getPrefs();
			var viewMode = StringUtils.parseEnum(prefs.getString("settings_ui_episodes_mode"), ViewMode.LIST);

			if(viewMode != this.viewMode) {
				this.viewMode = viewMode;

				recycler.setLayoutManager(switch(viewMode) {
					case LIST -> new LinearLayoutManager(requireContext());

					case GRID -> {
						var columnsCount = new AtomicInteger(3);
						var layoutManager = new GridLayoutManager(requireContext(), columnsCount.get());

						setOnApplyUiInsetsListener(recycler, insets -> {
							var padding = dpPx(recycler, 8);
							ViewUtil.setVerticalPadding(recycler, padding + padding * 2);
							ViewUtil.setHorizontalPadding(recycler, insets.left + padding, insets.right + padding);

							float columnSize = dpPx(recycler, 80);
							float freeSpace = getResources().getDisplayMetrics().widthPixels - (padding * 2) - insets.left - insets.right;
							columnsCount.set((int)(freeSpace / columnSize));
							layoutManager.setSpanCount(columnsCount.get());

							return true;
						});

						layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
							@Override
							public int getSpanSize(int position) {
								/* Don't ask. I don't know how it is working, so please don't ask about it. */
								return (concatAdapter.getItemViewType(position) == VIEW_TYPE_EPISODE) ? 1 : columnsCount.get();
							}
						});

						yield layoutManager;
					}
				});
			}
		}

		changeSettings = false;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		if(media == null) {
			media = requireArgument(this, "media");
		}

		providers = new ArrayList<>(stream(ExtensionsFactory.getExtensions__Deprecated(__Extension.FLAG_WORKING))
				.map(extension -> extension.getProviders(switch(media.getType()) {
					case TV, MOVIE -> ExtensionConstants.FEATURE_MEDIA_WATCH;
					case COMICS, BOOK -> ExtensionConstants.FEATURE_MEDIA_READ;
				}))
				.flatMap(NiceUtils::stream)
				.sorted().toList());

		sourcesDropdownAdapter = new ArrayListAdapter<>((item, recycled, parent) -> {
			if(recycled == null) {
				var inflater = LayoutInflater.from(parent.getContext());
				var binding = ItemListDropdownBinding.inflate(inflater, parent, false);
				recycled = binding.getRoot();
			}

			TextView title = recycled.findViewById(R.id.title);
			ImageView icon = recycled.findViewById(R.id.icon);

			title.setText(item.getName());

			var status = sourceStatuses.get(item);

			if(status != null) {
				var statusColor = status == ExtensionStatus.OK ? Color.GREEN : Color.RED;

				var iconRes = switch(status) {
					case OK -> R.drawable.ic_check;
					case BROKEN_PARSER -> R.drawable.ic_round_error_24;
					case SERVER_DOWN -> R.drawable.ic_round_block_24;
					case OFFLINE -> R.drawable.ic_round_signal_no_internet_24;
					case NOT_FOUND -> R.drawable.ic_zero;
					case NONE -> null;
				};

				if(iconRes != null) {
					icon.setImageResource(iconRes);
					icon.setVisibility(View.VISIBLE);
					ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(statusColor));
				} else {
					icon.setVisibility(View.GONE);
				}
			} else {
				icon.setVisibility(View.GONE);
			}

			return recycled;
		}, providers);

		var more = getString(R.string.manual_search);
		var titles = new ArrayList<>(List.of(media.getTitles()));
		titles.add(more);

		var titlesAdapter = new ArrayListAdapter<>((item, recycled, parent) -> {
			if(recycled == null) {
				var inflater = LayoutInflater.from(parent.getContext());
				var binding = ItemListDropdownBinding.inflate(inflater, parent, false);
				recycled = binding.getRoot();
			}

			TextView title = recycled.findViewById(R.id.title);
			title.setText(item);

			return recycled;
		}, titles);

		variantsAdapter.getBinding((binding) -> {
			binding.sourceDropdown.setAdapter(sourcesDropdownAdapter);
			binding.searchDropdown.setAdapter(titlesAdapter);

			binding.sourceDropdown.setOnItemClickListener((parent, _view, position, id) -> {
				searchId = null;
				searchTitle = null;

				episodesAdapter.setItems(null, null);
				autoChangeSource = false;
				autoChangeTitle = true;
				selectProvider(providers.get(position));
			});

			binding.searchDropdown.setOnItemClickListener((parent, _view, position, id) -> {
				var title = requireNonNull(titles.get(position));

				if(selectedSource == null) {
					if(title.equals(more)) {
						binding.searchDropdown.setText(queryFilter.getStringValue(), false);
					}

					toast("You haven't selected any source!", 1);
					return;
				}

				if(title.equals(more)) {
					binding.searchDropdown.setText(queryFilter.getStringValue(), false);

					var intent = new Intent(requireContext(), SearchActivity.class);
					intent.setAction(SearchActivity.ACTION_PICK_MEDIA);
					intent.putExtra(SearchActivity.EXTRA_GLOBAL_PROVIDER_ID, selectedSource.getGlobalId());
					intent.putExtra(SearchActivity.EXTRA_FILTERS, filters);

					Lifecycle.startActivityForResult(requireContext(), intent, REQUEST_CODE_PICK_MEDIA, (resultCode, result) -> {
						if(result == null) return;

						var media = (Media) result.getSerializableExtra(SearchActivity.RESULT_EXTRA_MEDIA);
						if(media == null) return;

						searchId = null;
						searchTitle = null;

						binding.searchDropdown.setText(media.getTitle(), false);
						queryFilter.setValue(media.getTitle());

						placeholderAdapter.getBinding(placeholder -> {
							placeholder.startLoading();
							placeholderAdapter.setEnabled(true);
							episodesAdapter.setItems(media, Collections.emptyList());

							autoChangeSource = false;
							autoChangeTitle = false;

							episodesAdapter.setItems(null, null);
							loadEpisodesFromSource(selectedSource, media);
						});
					});
				} else {
					searchId = null;
					searchTitle = null;

					episodesAdapter.setItems(null, null);
					autoChangeSource = false;
					autoChangeTitle = false;
					queryFilter.setValue(title);
					selectProvider(selectedSource);
				}
			});

			/*binding.options.setOnClickListener(v -> {
				var intent = new Intent(requireContext(), SettingsActivity.class);
				intent.putExtra("path", "settings_ui_episodes");
				startActivity(intent);
				changeSettings = true;
			});*/
		});

		this.queryFilter.setValue(media.getTitle());

		if(providers.isEmpty()) {
			handleExceptionUi(null, new ZeroResultsException("No extensions was found", R.string.no_extensions_found));
			variantsAdapter.setEnabled(false);
			return;
		}

		thread(() -> {
			var progress = getDatabase().getMediaProgressDao().get(media.getGlobalId());

			var mediaSource = NiceUtils.returnWith(() -> {
				try {
					return __ExtensionProvider.forGlobalId(media.getGlobalId());
				} catch(ExtensionNotInstalledException | ExtensionComponentMissingException e) {
					Log.e(TAG, "Source extension isn't installed!", e);
					return null;
				}
			});

			if(progress != null) {
				searchId = progress.lastId;
				searchTitle = progress.lastTitle;

				providers.sort((a, b) -> {
					if(a.getId().equals(progress.lastWatchSource)) return -1;
					if(b.getId().equals(progress.lastWatchSource)) return 1;

					if(mediaSource != null) {
						if(a.getId().equals(mediaSource.getId())) return -1;
						if(b.getId().equals(mediaSource.getId())) return 1;
					}

					return 0;
				});

				sourcesDropdownAdapter.setItems(providers);
			} else if(mediaSource != null) {
				providers.sort((a, b) -> {
					if(a.getId().equals(mediaSource.getId())) return -1;
					if(b.getId().equals(mediaSource.getId())) return 1;
					return 0;
				});

				sourcesDropdownAdapter.setItems(providers);
			}

			if(mediaSource != null) {
				mediaSource.getVideos(new SettingsList(
						new SettingsItem(SettingsItemType.INTEGER, ExtensionConstants.FILTER_PAGE, 0),
						new SettingsItem(SettingsItemType.JSON, ExtensionConstants.FILTER_MEDIA, Parser.toString(Media.class, media))
				)).addCallback(new AsyncFuture.Callback<>() {
					@Override
					public void onSuccess(List<? extends CatalogVideo> catalogEpisodes) {
						templateEpisodes = catalogEpisodes;
						currentSourceIndex = 0;
						runOnUiThread(() -> selectProvider(providers.get(0)));
					}

					@Override
					public void onFailure(Throwable e) {
						// Don't merge any data. Just load original data
						currentSourceIndex = 0;
						runOnUiThread(() -> selectProvider(providers.get(0)));
					}
				});
			} else {
				currentSourceIndex = 0;
				runOnUiThread(() -> selectProvider(providers.get(0)));
			}
		});
	}

	private void selectProvider(@NonNull __ExtensionProvider provider) {
		variantsAdapter.getBinding((binding) ->
				binding.sourceDropdown.setText(provider.getName(), false));

		loadEpisodesFromSource(provider);
		variantsAdapter.getBinding((binding) -> binding.variantWrapper.setVisibility(View.GONE));
	}

	private void loadEpisodesFromSource(@NonNull __ExtensionProvider source, Media media) {
		var myId = ++loadId;

		variantsAdapter.getBinding(binding -> runOnUiThread(() -> {
			binding.searchStatus.setText("Searching episodes for \"" + media.getTitle() + "\"...");
			binding.searchStatus.setOnClickListener(v -> MediaUtils.launchMediaActivity(requireContext(), media));
		}));

		source.getVideos(new SettingsList(
				new SettingsItem(SettingsItemType.INTEGER, ExtensionConstants.FILTER_PAGE, 0),
				new SettingsItem(SettingsItemType.JSON, ExtensionConstants.FILTER_MEDIA, Parser.toString(Media.class, media))
		)).addCallback(new AsyncFuture.Callback<>() {
			@Override
			public void onSuccess(List<? extends CatalogVideo> episodes) {
				if(source != selectedSource || myId != loadId) return;

				sourceStatuses.put(source, ExtensionStatus.OK);

				episodes = new ArrayList<>(episodes);
				episodes.sort(Comparator.comparing(CatalogVideo::getNumber));
				var finalEpisodes = episodes;

				if(templateEpisodes != null) {
					for(var episode : episodes) {
						var templateEpisode = stream(templateEpisodes)
								.filter(e -> e.getNumber() == episode.getNumber())
								.findFirst().orElse(null);

						if(templateEpisode == null) {
							continue;
						}

						if(episode.getBanner() == null) {
							episode.setBanner(templateEpisode.getBanner());
						}
					}
				}

				runOnUiThread(() -> {
					variantsAdapter.getBinding(binding -> {
						binding.searchStatus.setText("Selected \"" + media.getTitle() + "\"");
						binding.searchStatus.setOnClickListener(v -> MediaUtils.launchMediaActivity(requireContext(), media));
					});

					placeholderAdapter.setEnabled(false);
					episodesAdapter.setItems(media, finalEpisodes);
				});
			}

			@Override
			public void onFailure(@NonNull Throwable e) {
				if(source != selectedSource || myId != loadId) return;

				Log.e(TAG, "Failed to load episodes!", e);

				runOnUiThread(() -> {
					handleExceptionMark(source, e);
					if(autoSelectNextSource()) return;
					handleExceptionUi(source, e);
				});
			}
		});
	}

	private void loadEpisodesFromSource(@NonNull __ExtensionProvider source) {
		this.selectedSource = source;
		var myId = ++loadId;

		placeholderAdapter.setEnabled(true);
		placeholderAdapter.getBinding(EmptyView::startLoading);

		runOnUiThread(() -> {
			try {
				episodesAdapter.setItems(media, Collections.emptyList());
			} catch(IllegalStateException e) {
				Log.e(TAG, "Lets hope that the episodes adapter was just created ._.");
			}
		});

		var lastUsedTitleIndex = new AtomicInteger(0);

		if(autoChangeSource) {
			queryFilter.setValue(media.getTitle());
		}

		variantsAdapter.getBinding((binding) ->
				binding.searchDropdown.setText(queryFilter.getStringValue(), false));

		var foundMediaCallback = new AsyncFuture.Callback<CatalogSearchResults<? extends Media>>() {

			@Override
			public void onSuccess(@NonNull CatalogSearchResults<? extends Media> media) {
				if(source != selectedSource || myId != loadId) return;
				loadEpisodesFromSource(source, media.get(0));
			}

			@Override
			public void onFailure(Throwable e) {
				if(myId != loadId) return;

				Log.e(TAG, "Failed to search media!", e);

				runOnUiThread(() -> {
					var context = getContext();
					if(context == null) return;

					if(autoChangeTitle && media.getTitles() != null && lastUsedTitleIndex.get() < media.getTitles().length - 1) {
						var newIndex = lastUsedTitleIndex.incrementAndGet();
						queryFilter.setValue(media.getTitles()[newIndex]);
						source.searchMedia(filters).addCallback(this);

						variantsAdapter.getBinding(binding -> runOnUiThread(() -> {
							binding.searchStatus.setText("Searching for \"" + queryFilter.getStringValue() + "\"...");
							binding.searchStatus.setOnClickListener(null);
						}));

						variantsAdapter.getBinding((binding) -> binding.searchDropdown.setText(
								queryFilter.getStringValue(), false));
					} else {
						handleExceptionMark(source, e);
						if(autoSelectNextSource()) return;
						handleExceptionUi(source, e);
					}
				});
			}
		};

		var context = getContext();
		if(context == null) return;

		if(searchId != null) {
			variantsAdapter.getBinding(binding -> runOnUiThread(() -> {
				binding.searchStatus.setText("Searching for \"" + searchTitle + "\"...");
				binding.searchStatus.setOnClickListener(null);
			}));

			source.getMedia(searchId).addCallback(new AsyncFuture.Callback<>() {
				@Override
				public void onSuccess(Media media) {
					if(source != selectedSource || myId != loadId) return;

					media = new Media.Builder(media)
							.setTitles(searchTitle)
							.build();

					loadEpisodesFromSource(source, media);
					searchId = null;
				}

				@Override
				public void onFailure(Throwable e) {
					if(source != selectedSource || myId != loadId) return;

					variantsAdapter.getBinding(binding -> runOnUiThread(() -> {
						binding.searchStatus.setText("Searching for \"" + queryFilter.getStringValue() + "\"...");
						binding.searchStatus.setOnClickListener(null);
					}));

					source.searchMedia(filters).addCallback(foundMediaCallback);
				}
			});
		} else {
			variantsAdapter.getBinding(binding -> runOnUiThread(() -> {
				binding.searchStatus.setText("Searching for \"" + queryFilter.getStringValue() + "\"...");
				binding.searchStatus.setOnClickListener(null);
			}));

			source.searchMedia(filters).addCallback(foundMediaCallback);
		}
	}

	private boolean autoSelectNextSource() {
		if(!autoChangeSource) return false;

		currentSourceIndex++;

		if(currentSourceIndex >= providers.size()) {
			return false;
		}

		selectProvider(providers.get(currentSourceIndex));
		return true;
	}

	private void handleExceptionMark(__ExtensionProvider source, Throwable throwable) {
		if(source != selectedSource) return;
		var error = new ExceptionDescriptor(throwable);

		/*sourceStatuses.put(source, switch(ExceptionDescriptor.getReason(throwable)) {
			case SERVER_DOWN -> ExtensionStatus.SERVER_DOWN;
			case OTHER, UNIMPLEMENTED -> ExtensionStatus.BROKEN_PARSER;
		});*/

		if(throwable instanceof ZeroResultsException) sourceStatuses.put(source, ExtensionStatus.NOT_FOUND);
		else if(error.isNetworkException()) sourceStatuses.put(source, ExtensionStatus.OFFLINE);
		else sourceStatuses.put(source, ExtensionStatus.BROKEN_PARSER);
	}

	private void handleExceptionUi(__ExtensionProvider source, Throwable throwable) {
		if(source != selectedSource && source != null) return;
		var error = new ExceptionDescriptor(throwable);

		var context = getContext();
		if(context == null) return;

		variantsAdapter.getBinding(binding -> {
			binding.searchStatus.setOnClickListener(null);

			binding.searchStatus.setText(error.isUnknownException()
					? "Failed to load episodes list"
					: error.getMessage(context));
		});

		placeholderAdapter.getBinding(binding -> runOnUiThread(() -> {
			binding.setInfo(context, throwable);
			placeholderAdapter.setEnabled(true);
		}));
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		var layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

		variantsAdapter = SingleViewAdapter.fromBindingDynamic(parent ->
				LayoutWatchVariantsBinding.inflate(inflater, parent, false), VIEW_TYPE_VARIANTS);

		placeholderAdapter = SingleViewAdapter.fromBindingDynamic(parent ->
				new EmptyView(parent, false), VIEW_TYPE_ERROR);

		episodesAdapter = new MediaPlayEpisodesAdapter();
		episodesAdapter.setOnEpisodeSelectedListener(this);

		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.setIsolateViewTypes(false)
				.build();

		concatAdapter = new ConcatAdapter(config,
				variantsAdapter, episodesAdapter, placeholderAdapter);

		recycler = new RecyclerView(inflater.getContext());
		recycler.setLayoutManager(layoutManager);
		recycler.setAdapter(concatAdapter);

		recycler.setClipToPadding(false);
		setBottomPadding(recycler, dpPx(recycler, 12));

		setOnApplyUiInsetsListener(recycler, insets -> {
			setTopPadding(recycler, insets.top);
			setRightPadding(recycler, insets.right);
			return true;
		}, container);

		return recycler;
	}
}