package com.mrboomdev.awery.ui.fragments;

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
import com.mrboomdev.awery.catalog.provider.ExtensionProvider;
import com.mrboomdev.awery.catalog.provider.ExtensionsManager;
import com.mrboomdev.awery.catalog.provider.data.ExtensionProviderGroup;
import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.ui.activity.PlayerActivity;
import com.mrboomdev.awery.ui.adapter.MediaPlayEpisodesAdapter;
import com.mrboomdev.awery.util.ErrorUtil;
import com.mrboomdev.awery.util.ui.CustomArrayAdapter;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import ani.awery.R;
import ani.awery.databinding.ItemListDropdownBinding;
import ani.awery.databinding.LayoutLoadingBinding;
import ani.awery.databinding.MediaDetailsWatchVariantsBinding;

public class MediaPlayFragment extends Fragment implements MediaPlayEpisodesAdapter.OnEpisodeSelectedListener {
	private final String TAG = "MediaPlayFragment";
	private final Map<String, ExtensionStatus> sourceStatuses = new HashMap<>();
	private CatalogMedia media;
	private MediaPlayEpisodesAdapter episodesAdapter;
	private ExtensionProvider selectedSource;
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutLoadingBinding> placeholderAdapter;
	private SingleViewAdapter.BindingSingleViewAdapter<MediaDetailsWatchVariantsBinding> variantsAdapter;

	@Override
	public void onEpisodeSelected(@NonNull CatalogEpisode episode) {
		AweryApp.toast("Loading videos list...", 1);

		selectedSource.getVideos(episode, new ExtensionProvider.ResponseCallback<>() {
			@Override
			public void onSuccess(List<CatalogVideo> catalogVideos) {
				var video = catalogVideos.get(0);

				var intent = new Intent(requireContext(), PlayerActivity.class);
				intent.putExtra("url", video.getUrl());
				intent.putExtra("headers", video.getHeaders());
				startActivity(intent);
			}

			@Override
			public void onFailure(Throwable throwable) {
				var error = new ErrorUtil(throwable);

				if(!error.isGenericError()) {
					throwable.printStackTrace();
				}

				AweryApp.toast(error.getTitle(requireContext()), 1);
			}
		});
	}

	private enum ExtensionStatus {
		OK, OFFLINE, SERVER_DOWN, BROKEN_PARSER, NOT_FOUND, NONE
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
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		var extensions = new ArrayList<>(ExtensionsManager.getVideoExtensions());
		var sources = new ArrayList<ExtensionProvider>();

		for(var extension : extensions) {
			sources.addAll(extension.getProviders());
		}

		var groupedByLang = ExtensionProviderGroup.groupByLang(sources);
		var groupedByLangEntries = new ArrayList<>(groupedByLang.entrySet());

		var sourcesDropdownAdapter = new CustomArrayAdapter<>(view.getContext(), groupedByLangEntries, (
				Map.Entry<String, Map<String, ExtensionProvider>> itemEntry,
				View recycled,
				ViewGroup parent
		) -> {
			if(recycled == null) {
				var inflater = LayoutInflater.from(parent.getContext());
				var binding = ItemListDropdownBinding.inflate(inflater, parent, false);
				recycled = binding.getRoot();
			}

			if(recycled instanceof ViewGroup group) {
				var title = (TextView) group.getChildAt(0);
				var icon = (ImageView) group.getChildAt(1);

				title.setText(itemEntry.getKey());
				var status = sourceStatuses.get(itemEntry.getKey());

				if(status != null && itemEntry.getValue().size() == 1) {
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
		});

		variantsAdapter.getBinding((binding, didJustCreated) -> {
			binding.sourceDropdown.setAdapter(sourcesDropdownAdapter);

			binding.sourceDropdown.setOnItemClickListener((parent, _view, position, id) -> {
				var group = groupedByLangEntries.get(position);
				selectProvider(group, _view.getContext());
			});
		});

		selectProvider(groupedByLangEntries.get(0), view.getContext());
	}

	private void selectProvider(@NonNull Map.Entry<String, Map<String, ExtensionProvider>> sourcesMap, Context context) {
		variantsAdapter.getBinding((binding, didJustCreated) -> binding.sourceDropdown.setText(sourcesMap.getKey(), false));

		var sortedSourceEntries = sourcesMap.getValue().entrySet().stream().sorted((next, prev) -> {
			if(next.getKey().equals("en") && !prev.getKey().equals("en")) return -1;
			if(!next.getKey().equals("en") && prev.getKey().equals("en")) return 1;
			return 0;
		}).collect(Collectors.toList());

		var initialProvider = sortedSourceEntries.get(0);
		selectVariant(initialProvider.getValue(), initialProvider.getKey());

		if(sortedSourceEntries.size() > 1) {
			variantsAdapter.getBinding((binding, didJustCreated) -> {
				binding.variantWrapper.setVisibility(View.VISIBLE);

				var variantsDropdownAdapter = new CustomArrayAdapter<>(context, sortedSourceEntries, (
						Map.Entry<String, ExtensionProvider> itemEntry,
						View recycled,
						ViewGroup parent
				) -> {
					if(recycled == null) {
						var inflater = LayoutInflater.from(parent.getContext());
						var _binding = ItemListDropdownBinding.inflate(inflater, parent, false);
						recycled = _binding.getRoot();
					}

					if(recycled instanceof ViewGroup group) {
						var title = (TextView) group.getChildAt(0);
						title.setText(itemEntry.getKey());
					} else {
						throw new IllegalStateException("Recycled view is not a ViewGroup");
					}

					return recycled;
				});

				binding.variantDropdown.setAdapter(variantsDropdownAdapter);

				binding.variantDropdown.setOnItemClickListener((parent, _view, position, id) -> {
					var variant = sortedSourceEntries.get(position);
					if(variant == null) return;

					selectVariant(variant.getValue(), variant.getKey());
				});
			});

		} else {
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

		var searchParams = new ExtensionProvider.SearchParams.Builder()
				.setPage(0)
				.setQuery(media.titles.get(0));

		foundMediaCallback.set(new ExtensionProvider.ResponseCallback<>() {

			@Override
			public void onSuccess(@NonNull List<CatalogMedia> mediaList) {
				if(source != selectedSource) return;

				source.getEpisodes(0, mediaList.get(0), new ExtensionProvider.ResponseCallback<>() {
					@Override
					public void onSuccess(List<CatalogEpisode> episodes) {
						if(source != selectedSource) return;

						sourceStatuses.put(source.getName(), ExtensionStatus.OK);

						requireActivity().runOnUiThread(() -> {
							placeholderAdapter.setEnabled(false);
							episodesAdapter.setItems(episodes);
						});
					}

					@Override
					public void onFailure(@NonNull Throwable e) {
						handleException(source, e);
					}
				});
			}

			@Override
			public void onFailure(Throwable e) {
				var callback = foundMediaCallback.get();
				if(callback == null) return;

				if(e != ErrorUtil.ZERO_RESULTS) {
					handleException(source, e);
					return;
				}

				if(lastUsedTitleIndex.get() < media.titles.size() - 1) {
					var newIndex = lastUsedTitleIndex.incrementAndGet();
					searchParams.setQuery(media.titles.get(newIndex));
					source.search(searchParams.build(), callback);
				} else {
					AweryApp.runOnUiThread(() -> placeholderAdapter.getBinding((binding, didJustCreated) -> {
						binding.title.setText(R.string.nothing_found);
						binding.message.setText(R.string.tried_all_titles);
						binding.progressBar.setVisibility(View.GONE);
						binding.info.setVisibility(View.VISIBLE);
						placeholderAdapter.setEnabledSuperForce(true);
					}));
				}
			}
		});

		source.search(searchParams.build(), foundMediaCallback.get());
	}

	private void handleException(ExtensionProvider source, Throwable throwable) {
		if(source != selectedSource) return;
		var error = new ErrorUtil(throwable);

		if(!error.isGenericError()) {
			throwable.printStackTrace();
		}

		if(error.isProgramException()) {
			sourceStatuses.put(source.getName(), ExtensionStatus.BROKEN_PARSER);
		}

		placeholderAdapter.getBinding((binding, didJustCreated) -> AweryApp.runOnUiThread(() -> {
			binding.title.setText(error.getTitle(requireContext()));
			binding.message.setText(error.getMessage(requireContext()));

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
			var binding = MediaDetailsWatchVariantsBinding.inflate(inflater, parent, false);

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