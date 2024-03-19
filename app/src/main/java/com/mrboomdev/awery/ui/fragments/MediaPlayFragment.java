package com.mrboomdev.awery.ui.fragments;

import android.app.Activity;
import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionProviderChild;
import com.mrboomdev.awery.extensions.ExtensionProviderGroup;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.support.template.CatalogEpisode;
import com.mrboomdev.awery.extensions.support.template.CatalogFilter;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.databinding.ItemListDropdownBinding;
import com.mrboomdev.awery.databinding.LayoutLoadingBinding;
import com.mrboomdev.awery.databinding.LayoutWatchVariantsBinding;
import com.mrboomdev.awery.ui.activity.player.PlayerActivity;
import com.mrboomdev.awery.ui.adapter.MediaPlayEpisodesAdapter;
import com.mrboomdev.awery.util.CachedValue;
import com.mrboomdev.awery.util.TranslationUtil;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.CustomArrayAdapter;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MediaPlayFragment extends Fragment implements MediaPlayEpisodesAdapter.OnEpisodeSelectedListener {
	private final String TAG = "MediaPlayFragment";
	private final Map<ExtensionProvider, ExtensionStatus> sourceStatuses = new HashMap<>();
	private final CachedValue<ExtensionProviderGroup, List<ExtensionProvider>> currentGroupProviders = new CachedValue<>();
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutLoadingBinding> placeholderAdapter;
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutWatchVariantsBinding> variantsAdapter;
	private List<ExtensionProvider> providers;
	private MediaPlayEpisodesAdapter episodesAdapter;
	private ExtensionProvider selectedSource;
	private ExtensionProviderGroup selectedSourceGroup;
	private CatalogMedia media;
	private boolean autoChangeSource = true;
	private int currentSourceIndex = 0, currentGroupSourceIndex = 0;

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("media", media.toString());
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			var adapter = CatalogMedia.getJsonAdapter();

			try {
				var mediaJson = savedInstanceState.getString("media");
				if(mediaJson == null) return;

				var media = adapter.fromJson(mediaJson);

				if(media != null) {
					setMedia(media);
				} else {
					throw new IOException("Failed to restore media!");
				}
			} catch(IOException e) {
				Log.e(TAG, "Failed to restore media!", e);
				AweryApp.toast("Failed to restore media!", 1);
			}
		}
	}

	@Override
	public void onEpisodeSelected(@NonNull CatalogEpisode episode, @NonNull ArrayList<CatalogEpisode> episodes) {
		PlayerActivity.selectSource(selectedSource);

		var intent = new Intent(requireContext(), PlayerActivity.class);
		intent.putExtra("episode", episode);
		intent.putParcelableArrayListExtra("episodes", episodes);
		startActivity(intent);
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

		if(providers == null) {
			return;
		}

		if(providers.isEmpty()) {
			handleExceptionUi(null, new ZeroResultsException());
			variantsAdapter.setEnabled(false);
			return;
		}

		selectProvider(providers.get(0));
	}

	private String getProviderName(ExtensionProvider provider) {
		if(provider instanceof ExtensionProviderChild child && child.getProviderParent().areAllWithSameName()) {
			return TranslationUtil.getTranslatedLangName(requireContext(), provider.getLang());
		}

		return provider.getName();
	}

	@NonNull
	private View bindDropdownItem(ExtensionProvider item, View recycled, ViewGroup parent) {
		if(recycled == null) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = ItemListDropdownBinding.inflate(inflater, parent, false);
			recycled = binding.getRoot();
		}

		if(recycled instanceof ViewGroup viewGroup) {
			var title = (TextView) viewGroup.getChildAt(0);
			title.setText(getProviderName(item));

			var icon = (ImageView) viewGroup.getChildAt(1);
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
		} else {
			throw new IllegalStateException("Recycled view is not a ViewGroup");
		}

		return recycled;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var flags = Extension.FLAG_VIDEO_EXTENSION | Extension.FLAG_WORKING;
		providers = new ArrayList<>(ExtensionsFactory.getProviders(flags));
		Collections.sort(providers);

		var sourcesDropdownAdapter = new CustomArrayAdapter<>(providers, this::bindDropdownItem);

		variantsAdapter.getBinding((binding, didJustCreated) -> {
			binding.sourceDropdown.setAdapter(sourcesDropdownAdapter);

			binding.sourceDropdown.setOnItemClickListener((parent, _view, position, id) -> {
				selectProvider(providers.get(position));
				autoChangeSource = false;
			});
		});

		setMedia(media);
	}

	private void selectProvider(@NonNull ExtensionProvider provider) {
		variantsAdapter.getBinding((binding, didJustCreated) ->
				binding.sourceDropdown.setText(provider.getName(), false));

		if(provider instanceof ExtensionProviderGroup group) {
			variantsAdapter.getBinding((binding, didJustCreated) -> {
				var variants = new ArrayList<>(group.getProviders());
				currentGroupProviders.set(group, variants);
				Collections.sort(variants);

				var variantsDropdownAdapter = new CustomArrayAdapter<>(variants, this::bindDropdownItem);

				binding.variantDropdown.setOnItemClickListener((parent, _view, position, id) -> {
					var variant = variants.get(position);
					if(variant == null) return;

					autoChangeSource = false;
					selectVariant(variant, getProviderName(variant));
				});

				binding.variantDropdown.setAdapter(variantsDropdownAdapter);
				binding.variantWrapper.setVisibility(View.VISIBLE);
				selectVariant(variants.get(0), getProviderName(variants.get(0)));
			});
		} else {
			selectVariant(provider, null);
			variantsAdapter.getBinding((binding, didJustCreated) ->
					binding.variantWrapper.setVisibility(View.GONE));
		}
	}

	private void selectVariant(ExtensionProvider provider, String variant) {
		variantsAdapter.getBinding((binding, didJustCreated) -> {
			binding.variantDropdown.setText(variant, false);
			loadEpisodesFromSource(provider);
		});
	}

	private void loadEpisodesFromSource(@NonNull ExtensionProvider source) {
		placeholderAdapter.setEnabled(true);
		this.selectedSource = source;

		if(source instanceof ExtensionProviderChild child) {
			selectedSourceGroup = child.getProviderParent();
		} else {
			selectedSourceGroup = null;
		}

		placeholderAdapter.getBinding((binding, didJustCreated) -> {
			binding.info.setVisibility(View.GONE);
			binding.progressBar.setVisibility(View.VISIBLE);
		});

		AweryApp.runOnUiThread(() -> {
			try {
				episodesAdapter.setItems(Collections.emptyList());
			} catch(IllegalStateException e) {
				Log.e(TAG, "Lets hope that the episodes adapter was just created ._.");
			}
		});

		var lastUsedTitleIndex = new AtomicInteger(0);
		var foundMediaCallback = new AtomicReference<ExtensionProvider.ResponseCallback<List<CatalogMedia>>>();

		var searchParams = new CatalogFilter.Builder()
				.setPage(0)
				.setQuery(media.titles.get(0));

		variantsAdapter.getBinding((binding, didJustCreated) ->
				binding.searchDropdown.setText(searchParams.getQuery(), false));

		foundMediaCallback.set(new ExtensionProvider.ResponseCallback<>() {

			@Override
			public void onSuccess(@NonNull List<CatalogMedia> mediaList) {
				if(source != selectedSource) return;

				source.getEpisodes(0, mediaList.get(0), new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(List<CatalogEpisode> episodes) {
						if(source != selectedSource) return;
						Activity activity;

						try {
							activity = requireActivity();
						} catch(IllegalStateException e) {
							return;
						}

						sourceStatuses.put(source, ExtensionStatus.OK);

						activity.runOnUiThread(() -> {
							placeholderAdapter.setEnabled(false);
							episodesAdapter.setItems(episodes);
						});
					}

					@Override
					public void onFailure(@NonNull Throwable e) {
						handleExceptionMark(source, e);
						if(autoSelectNextSource()) return;
						handleExceptionUi(source, e);
					}
				});
			}

			@Override
			public void onFailure(Throwable e) {
				var callback = foundMediaCallback.get();
				if(callback == null) return;

				AweryApp.runOnUiThread(() -> {
					if(lastUsedTitleIndex.get() < media.titles.size() - 1) {
						var newIndex = lastUsedTitleIndex.incrementAndGet();
						searchParams.setQuery(media.titles.get(newIndex));
						source.search(searchParams.build(), callback);

						variantsAdapter.getBinding((binding, didJustCreated) ->
								binding.searchDropdown.setText(searchParams.getQuery(), false));
					} else {
						handleExceptionMark(source, e);
						if(autoSelectNextSource()) return;
						handleExceptionUi(source, e);
					}
				});
			}
		});

		source.search(searchParams.build(), foundMediaCallback.get());
	}

	private boolean autoSelectNextSource() {
		if(!autoChangeSource) return false;

		if(selectedSourceGroup != null) {
			var groupItems = currentGroupProviders.get(selectedSourceGroup);
			currentGroupSourceIndex++;

			if(groupItems == null) {
				throw new IllegalStateException("Group's " + selectedSourceGroup + " cache doesn't exist.");
			}

			if(currentGroupSourceIndex >= groupItems.size()) {
				currentGroupSourceIndex = 0;
				selectedSourceGroup = null;
				return autoSelectNextSource();
			}

			selectProvider(groupItems.get(currentGroupSourceIndex));
			return true;
		}

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

		if(!error.isNetworkException()) {
			sourceStatuses.put(source, ExtensionStatus.BROKEN_PARSER);
		} else if(throwable instanceof ZeroResultsException) {
			sourceStatuses.put(source, ExtensionStatus.NOT_FOUND);
		} else {
			sourceStatuses.put(source, ExtensionStatus.OFFLINE);
		}

		if(source instanceof ExtensionProviderChild child) {
			var parent = child.getProviderParent();
			enum AllAre { OK, BAD, UNKNOWN }
			var status = AllAre.UNKNOWN;

			for(var provider : parent.getProviders()) {
				var providerStatus = sourceStatuses.get(provider);

				if(providerStatus == null || providerStatus.isUnknown()) {
					status = AllAre.UNKNOWN;
					break;
				}

				if(providerStatus.isGood() && status == AllAre.BAD) {
					status = AllAre.UNKNOWN;
					break;
				}

				if(providerStatus.isBad() && status == AllAre.OK) {
					status = AllAre.UNKNOWN;
					break;
				}

				status = providerStatus.isGood() ? AllAre.OK : AllAre.BAD;
			}

			if(status == AllAre.OK) {
				sourceStatuses.put(parent, ExtensionStatus.OK);
			} else if(status == AllAre.BAD) {
				sourceStatuses.put(parent, ExtensionStatus.BROKEN_PARSER);
			}
		}
	}

	private void handleExceptionUi(ExtensionProvider source, Throwable throwable) {
		if(source != selectedSource && source != null) return;
		var error = new ExceptionDescriptor(throwable);
		Context context;

		try {
			context = requireContext();
		} catch(IllegalStateException e) {
			Log.e(TAG, "Failed to get context. Pray that we just restored the fragment.");
			return;
		}

		placeholderAdapter.getBinding((binding, didJustCreated) -> AweryApp.runOnUiThread(() -> {
			binding.title.setText(error.getTitle(context));
			binding.message.setText(error.getMessage(context));

			binding.info.setVisibility(View.VISIBLE);
			binding.progressBar.setVisibility(View.GONE);

			placeholderAdapter.setEnabled(true);
		}));
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		var layoutManager = new LinearLayoutManager(inflater.getContext(), LinearLayoutManager.VERTICAL, false);

		variantsAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var binding = LayoutWatchVariantsBinding.inflate(inflater, parent, false);

			ViewUtil.setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
				ViewUtil.setTopPadding(binding.getRoot(), insets.top);
				ViewUtil.setRightPadding(binding.getRoot(), insets.right);
			}, container);

			return binding;
		});

		placeholderAdapter = SingleViewAdapter.fromBindingDynamic(parent ->
				LayoutLoadingBinding.inflate(inflater, parent, false));

		episodesAdapter = new MediaPlayEpisodesAdapter();
		episodesAdapter.setOnEpisodeSelectedListener(this);

		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		ConcatAdapter concatAdapter = new ConcatAdapter(config, variantsAdapter, episodesAdapter, placeholderAdapter);

		var recycler = new RecyclerView(inflater.getContext());
		recycler.setLayoutManager(layoutManager);
		recycler.setAdapter(concatAdapter);
		return recycler;
	}
}