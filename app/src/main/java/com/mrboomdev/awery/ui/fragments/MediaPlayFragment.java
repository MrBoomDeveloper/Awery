package com.mrboomdev.awery.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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
import com.mrboomdev.awery.catalog.template.CatalogEpisode;
import com.mrboomdev.awery.catalog.provider.data.ExtensionProviderGroup;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.ui.adapter.MediaPlayEpisodesAdapter;
import com.mrboomdev.awery.util.ui.CustomArrayAdapter;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import ani.awery.R;
import ani.awery.databinding.ItemListDropdownBinding;
import ani.awery.databinding.MediaDetailsWatchVariantsBinding;

public class MediaPlayFragment extends Fragment {
	private final Map<String, ExtensionStatus> sourceStatuses = new HashMap<>();
	private CatalogMedia media;
	private ConcatAdapter concatAdapter;
	private MediaPlayEpisodesAdapter episodesAdapter;
	private MediaDetailsWatchVariantsBinding variantsBinding;
	private ExtensionProvider selectedSource;

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

		variantsBinding.sourceDropdown.setAdapter(sourcesDropdownAdapter);

		variantsBinding.sourceDropdown.setOnItemClickListener((parent, _view, position, id) -> {
			var group = groupedByLangEntries.get(position);
			selectProvider(group, _view.getContext());
		});

		selectProvider(groupedByLangEntries.get(0), view.getContext());
	}

	private void selectProvider(@NonNull Map.Entry<String, Map<String, ExtensionProvider>> sourcesMap, Context context) {
		variantsBinding.sourceDropdown.setText(sourcesMap.getKey(), false);

		var sortedSourceEntries = sourcesMap.getValue().entrySet().stream().sorted((next, prev) -> {
			if(next.getKey().equals("en") && !prev.getKey().equals("en")) return -1;
			if(!next.getKey().equals("en") && prev.getKey().equals("en")) return 1;
			return 0;
		}).collect(Collectors.toList());

		var initialProvider = sortedSourceEntries.get(0);
		selectVariant(initialProvider.getValue(), initialProvider.getKey());

		if(sortedSourceEntries.size() > 1) {
			variantsBinding.variantWrapper.setVisibility(View.VISIBLE);

			var variantsDropdownAdapter = new CustomArrayAdapter<>(context, sortedSourceEntries, (
					Map.Entry<String, ExtensionProvider> itemEntry,
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
					title.setText(itemEntry.getKey());
				} else {
					throw new IllegalStateException("Recycled view is not a ViewGroup");
				}

				return recycled;
			});

			variantsBinding.variantDropdown.setAdapter(variantsDropdownAdapter);

			variantsBinding.variantDropdown.setOnItemClickListener((parent, _view, position, id) -> {
				var variant = sortedSourceEntries.get(position);
				if(variant == null) return;

				selectVariant(variant.getValue(), variant.getKey());
			});
		} else {
			variantsBinding.variantWrapper.setVisibility(View.GONE);
		}
	}

	private void selectVariant(ExtensionProvider provider, String variant) {
		variantsBinding.variantDropdown.setText(variant, false);
		loadEpisodesFromSource(provider);
	}

	private void loadEpisodesFromSource(@NonNull ExtensionProvider source) {
		this.selectedSource = source;
		episodesAdapter.setItems(Collections.emptyList());

		source.getEpisodes(0, media, new ExtensionProvider.ResponseCallback<>() {
			@Override
			public void onSuccess(Collection<CatalogEpisode> episodes) {
				if(source != selectedSource) return;

				sourceStatuses.put(source.getName(), ExtensionStatus.OK);
				episodesAdapter.setItems(episodes);
			}

			@Override
			public void onFailure(@NonNull Throwable e) {
				e.printStackTrace();

				if(e == ExtensionProvider.CONNECTION_FAILED) {
					AweryApp.toast("Failed to connect to the server!", 1);
					sourceStatuses.put(source.getName(), ExtensionStatus.OFFLINE);
				} else if(e == ExtensionProvider.ZERO_RESULTS) {
					AweryApp.toast("Didn't found any episodes!", 0);
					sourceStatuses.put(source.getName(), ExtensionStatus.NOT_FOUND);
				} else {
					AweryApp.toast("Failed to load episodes list!", 1);
					sourceStatuses.put(source.getName(), ExtensionStatus.BROKEN_PARSER);
				}
			}
		});
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		var layoutManager = new LinearLayoutManager(inflater.getContext(), LinearLayoutManager.VERTICAL, false);

		var variantsLayoutParams = new RecyclerView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		variantsBinding = MediaDetailsWatchVariantsBinding.inflate(inflater, container, false);
		var headerAdapter = SingleViewAdapter.fromView(variantsBinding.getRoot(), 0, variantsLayoutParams);

		episodesAdapter = new MediaPlayEpisodesAdapter();

		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		concatAdapter = new ConcatAdapter(config, headerAdapter, episodesAdapter);

		ViewUtil.setOnApplyUiInsetsListener(variantsBinding.getRoot(), insets -> {
			ViewUtil.setTopPadding(variantsBinding.getRoot(), insets.top);
			ViewUtil.setRightPadding(variantsBinding.getRoot(), insets.right);
		}, container);

		var recycler = new RecyclerView(inflater.getContext());
		recycler.setLayoutManager(layoutManager);
		recycler.setAdapter(concatAdapter);
		return recycler;
	}
}