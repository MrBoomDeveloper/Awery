package com.mrboomdev.awery.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.provider.Extension;
import com.mrboomdev.awery.catalog.provider.ExtensionProvider;
import com.mrboomdev.awery.catalog.provider.ExtensionsManager;
import com.mrboomdev.awery.catalog.provider.data.Episode;
import com.mrboomdev.awery.catalog.provider.data.ExtensionProviderGroup;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ui.CustomArrayAdapter;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ani.awery.R;
import ani.awery.databinding.HeaderLayoutBinding;
import ani.awery.databinding.MediaDetailsWatchVariantsBinding;
import ani.awery.databinding.MenuDropdownItemBinding;

public class MediaPlayFragment extends Fragment {
	private final ConcatAdapter concatAdapter;
	private final CatalogMedia media;
	private MediaDetailsWatchVariantsBinding variantsBinding;

	public MediaPlayFragment(CatalogMedia media) {
		this.media = media;

		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		concatAdapter = new ConcatAdapter(config);
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
				Map.Entry<String, Map<String, ExtensionProvider>> item,
				View recycled,
				ViewGroup parent
		) -> {
			if(recycled == null) {
				var inflater = LayoutInflater.from(parent.getContext());
				var binding = MenuDropdownItemBinding.inflate(inflater, parent, false);
				recycled = binding.getRoot();
			}

			if(recycled instanceof ViewGroup group) {
				var title = (TextView) group.getChildAt(0);
				var icon = (ImageView) group.getChildAt(1);

				title.setText(item.getKey());
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
		variantsBinding.variantDropdown.setText(initialProvider.getKey(), false);
		loadEpisodesFromSource(initialProvider.getValue());

		if(sortedSourceEntries.size() > 1) {
			variantsBinding.variantWrapper.setVisibility(View.VISIBLE);

			var variantsDropdownAdapter = new CustomArrayAdapter<>(context, sortedSourceEntries, (
					Map.Entry<String, ExtensionProvider> item,
					View recycled,
					ViewGroup parent
			) -> {
				if(recycled == null) {
					var inflater = LayoutInflater.from(parent.getContext());
					var binding = MenuDropdownItemBinding.inflate(inflater, parent, false);
					recycled = binding.getRoot();
				}

				if(recycled instanceof ViewGroup group) {
					var title = (TextView) group.getChildAt(0);
					var icon = (ImageView) group.getChildAt(1);

					title.setText(item.getKey());
				} else {
					throw new IllegalStateException("Recycled view is not a ViewGroup");
				}

				return recycled;
			});

			variantsBinding.variantDropdown.setAdapter(variantsDropdownAdapter);

			variantsBinding.variantDropdown.setOnItemClickListener((parent, _view, position, id) -> {
				var variant = sortedSourceEntries.get(position);
				if(variant == null) return;

				loadEpisodesFromSource(variant.getValue());
				variantsBinding.variantDropdown.setText(variant.getKey(), false);
			});
		} else {
			variantsBinding.variantWrapper.setVisibility(View.GONE);
		}
	}

	private void loadEpisodesFromSource(@NonNull ExtensionProvider source) {
		source.getEpisodes(0, media, new ExtensionProvider.ResponseCallback<>() {
			@Override
			public void onSuccess(Collection<Episode> episodes) {
				if(episodes == ExtensionProvider.CONNECTION_FAILED_LIST) {
					AweryApp.toast("Failed to connect to the server!", 0);
					return;
				}

				if(episodes.isEmpty()) {
					AweryApp.toast("Didn't find any episodes!", 0);
					return;
				}

				AweryApp.toast("yay", 0);
				System.out.println(episodes);
			}

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				AweryApp.toast("Failed to load episodes list!", 1);
			}
		});
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		var layoutManager = new FlexboxLayoutManager(inflater.getContext());

		variantsBinding = MediaDetailsWatchVariantsBinding.inflate(inflater, container, false);
		SingleViewAdapter headerAdapter = SingleViewAdapter.fromView(variantsBinding.getRoot());
		concatAdapter.addAdapter(headerAdapter);

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