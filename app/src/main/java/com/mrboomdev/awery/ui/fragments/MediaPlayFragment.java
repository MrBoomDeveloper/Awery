package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.data.Constants.alwaysTrue;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

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
import com.mrboomdev.awery.app.AweryLifecycle;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.ItemListDropdownBinding;
import com.mrboomdev.awery.databinding.LayoutWatchVariantsBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.ui.activity.search.SearchActivity;
import com.mrboomdev.awery.ui.activity.player.PlayerActivity;
import com.mrboomdev.awery.ui.adapter.MediaPlayEpisodesAdapter;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.ArrayListAdapter;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaPlayFragment extends Fragment implements MediaPlayEpisodesAdapter.OnEpisodeSelectedListener {
	private static final int REQUEST_CODE_PICK_MEDIA = AweryLifecycle.getActivityResultCode();
	public static final int VIEW_TYPE_VARIANTS = 1;
	public static final int VIEW_TYPE_ERROR = 2;
	public static final int VIEW_TYPE_EPISODE = 3;
	private static final String TAG = "MediaPlayFragment";
	private final Map<ExtensionProvider, ExtensionStatus> sourceStatuses = new HashMap<>();
	private final SettingsItem queryFilter = new SettingsItem(SettingsItemType.STRING, ExtensionProvider.FILTER_QUERY);
	private final List<SettingsItem> filters = List.of(queryFilter,
			new SettingsItem(SettingsItemType.INTEGER, ExtensionProvider.FILTER_PAGE, 0));
	private SingleViewAdapter.BindingSingleViewAdapter<EmptyView> placeholderAdapter;
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutWatchVariantsBinding> variantsAdapter;
	private ArrayListAdapter<ExtensionProvider> sourcesDropdownAdapter;
	private ConcatAdapter concatAdapter;
	private List<? extends CatalogEpisode> templateEpisodes;
	private List<ExtensionProvider> providers;
	private MediaPlayEpisodesAdapter episodesAdapter;
	private RecyclerView recycler;
	private ExtensionProvider selectedSource;
	private ViewMode viewMode;
	private CatalogMedia media;
	private String searchId, searchTitle;
	private boolean autoChangeSource = true, autoChangeTitle = true, changeSettings = true;
	private int currentSourceIndex = 0;
	private long loadId;

	public enum ViewMode { GRID, LIST }

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("media", media.toString());
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			try {
				var mediaJson = savedInstanceState.getString("media");
				if(mediaJson == null) return;

				setMedia(Parser.fromString(CatalogMedia.class, mediaJson));
			} catch(IOException e) {
				Log.e(TAG, "Failed to restore media!", e);
				toast("Failed to restore media!", 1);
			}
		}
	}

	@Override
	public void onEpisodeSelected(@NonNull CatalogEpisode episode, @NonNull List<CatalogEpisode> episodes) {
		PlayerActivity.selectSource(selectedSource);

		var intent = new Intent(requireContext(), PlayerActivity.class);
		intent.putExtra("episode", episode);
		intent.putExtra("episodes", (Serializable) episodes);
		startActivity(intent);

		new Thread(() -> {
			var dao = getDatabase().getMediaProgressDao();

			var progress = dao.get(media.globalId);
			if(progress == null) progress = new CatalogMediaProgress(media.globalId);

			progress.lastWatchSource = selectedSource.getId();

			var foundMedia = episodesAdapter.getMedia();
			progress.lastId = foundMedia.getId();
			progress.lastTitle = foundMedia.getTitle();

			dao.insert(progress);
		}).start();
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

	public MediaPlayFragment(CatalogMedia media) {
		setMedia(media);
	}

	public MediaPlayFragment() {
		this(null);
	}

	public void setMedia(CatalogMedia media) {
		if(media == null) return;

		this.media = media;
		this.queryFilter.setValue(media.getTitle());

		if(providers == null) {
			return;
		}

		if(providers.isEmpty()) {
			handleExceptionUi(null, new ZeroResultsException("No extensions was found", R.string.no_extensions_found));
			variantsAdapter.setEnabled(false);
			return;
		}

		new Thread(() -> {
			var progress = getDatabase().getMediaProgressDao().get(media.globalId);
			var mediaSource = ExtensionsFactory.getExtensionProvider(Extension.FLAG_WORKING, media.globalId);

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
				mediaSource.getEpisodes(0, media, new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(List<? extends CatalogEpisode> catalogEpisodes) {
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
		}).start();
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

						ViewUtil.setOnApplyUiInsetsListener(recycler, insets -> {
							var padding = ViewUtil.dpPx(8);
							ViewUtil.setVerticalPadding(recycler, padding + padding * 2);
							ViewUtil.setHorizontalPadding(recycler, insets.left + padding, insets.right + padding);

							float columnSize = ViewUtil.dpPx(80);
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
		providers = new ArrayList<>(stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
				.map(extension -> extension.getProviders(ExtensionProvider.FEATURE_MEDIA_WATCH))
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
					case NOT_FOUND -> R.drawable.round_exposure_zero_24;
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

		var more = "Search manually";
		var titles = new ArrayList<>(media.titles);
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
				var title = titles.get(position);

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
					intent.putExtra("source", selectedSource.getId());
					intent.putExtra("filters", (Serializable) filters);
					intent.putExtra("select", true);

					AweryLifecycle.startActivityForResult(requireContext(), intent, REQUEST_CODE_PICK_MEDIA, (resultCode, result) -> {
						if(result == null) return;

						var mediaJson = result.getStringExtra("media");
						if(mediaJson == null) return;

						try {
							searchId = null;
							searchTitle = null;

							var media = Parser.fromString(CatalogMedia.class, mediaJson);
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
						} catch(IOException e) {
							CrashHandler.showErrorDialog(requireContext(), e);
						}
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

		setMedia(media);
	}

	private void selectProvider(@NonNull ExtensionProvider provider) {
		variantsAdapter.getBinding((binding) ->
				binding.sourceDropdown.setText(provider.getName(), false));

		loadEpisodesFromSource(provider);
		variantsAdapter.getBinding((binding) -> binding.variantWrapper.setVisibility(View.GONE));
	}

	private void loadEpisodesFromSource(@NonNull ExtensionProvider source, CatalogMedia media) {
		var myId = ++loadId;

		variantsAdapter.getBinding(binding -> runOnUiThread(() -> {
			binding.searchStatus.setText("Searching episodes for \"" + media.getTitle() + "\"...");
			binding.searchStatus.setOnClickListener(v -> MediaUtils.launchMediaActivity(requireContext(), media));
		}));

		source.getEpisodes(0, media, new ExtensionProvider.ResponseCallback<>() {
			@Override
			public void onSuccess(List<? extends CatalogEpisode> episodes) {
				if(source != selectedSource || myId != loadId) return;

				sourceStatuses.put(source, ExtensionStatus.OK);

				episodes = new ArrayList<>(episodes);
				episodes.sort(Comparator.comparing(CatalogEpisode::getNumber));
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

	private void loadEpisodesFromSource(@NonNull ExtensionProvider source) {
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
			queryFilter.setValue(media.titles.get(0));
		}

		variantsAdapter.getBinding((binding) ->
				binding.searchDropdown.setText(queryFilter.getStringValue(), false));

		var foundMediaCallback = new ExtensionProvider.ResponseCallback<CatalogSearchResults<? extends CatalogMedia>>() {

			@Override
			public void onSuccess(@NonNull CatalogSearchResults<? extends CatalogMedia> media) {
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

					if(autoChangeTitle && lastUsedTitleIndex.get() < media.titles.size() - 1) {
						var newIndex = lastUsedTitleIndex.incrementAndGet();
						queryFilter.setValue(media.titles.get(newIndex));
						source.searchMedia(context, filters, this);

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

			source.getMedia(requireContext(), searchId, new ExtensionProvider.ResponseCallback<>() {
				@Override
				public void onSuccess(CatalogMedia media) {
					if(source != selectedSource || myId != loadId) return;

					media.setTitle(searchTitle);
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

					source.searchMedia(context, filters, foundMediaCallback);
				}
			});
		} else {
			variantsAdapter.getBinding(binding -> runOnUiThread(() -> {
				binding.searchStatus.setText("Searching for \"" + queryFilter.getStringValue() + "\"...");
				binding.searchStatus.setOnClickListener(null);
			}));

			source.searchMedia(context, filters, foundMediaCallback);
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

	private void handleExceptionMark(ExtensionProvider source, Throwable throwable) {
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

	private void handleExceptionUi(ExtensionProvider source, Throwable throwable) {
		if(source != selectedSource && source != null) return;
		var error = new ExceptionDescriptor(throwable);

		var context = getContext();
		if(context == null) return;

		variantsAdapter.getBinding(binding -> {
			binding.searchStatus.setText(error.getMessage(context));
			binding.searchStatus.setOnClickListener(null);
		});

		placeholderAdapter.getBinding(binding -> runOnUiThread(() -> {
			binding.setInfo(error.getTitle(context), error.getMessage(context));
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
		setBottomPadding(recycler, dpPx(12));

		ViewUtil.setOnApplyUiInsetsListener(recycler, insets -> {
			setTopPadding(recycler, insets.top);
			setRightPadding(recycler, insets.right);
			return true;
		}, container);

		return recycler;
	}
}