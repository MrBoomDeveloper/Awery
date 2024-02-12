package com.mrboomdev.awery.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;

import com.mrboomdev.awery.ui.activity.SettingsActivity;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import org.jetbrains.annotations.Contract;

import ani.awery.databinding.HeaderLayoutBinding;
import ani.awery.databinding.MediaCatalogFragmentBinding;
import ani.awery.media.SearchActivity;

public class MediaCatalogFragment extends Fragment {
	private final HeaderAdapter header = new HeaderAdapter();
	private final ConcatAdapter concatAdapter;
	private MediaCatalogFragmentBinding binding;

	public MediaCatalogFragment() {
		var config = new ConcatAdapter.Config.Builder()
				.setIsolateViewTypes(true)
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		concatAdapter = new ConcatAdapter(config);
	}

	public void setupHeader(@NonNull HeaderLayoutBinding header) {
		header.search.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SearchActivity.class);
			startActivity(intent);
		});

		header.settingsWrapper.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SettingsActivity.class);
			startActivity(intent);
		});
	}

	public MediaCatalogFragmentBinding getBinding() {
		return binding;
	}

	public HeaderAdapter getHeaderAdapter() {
		return header;
	}

	public ConcatAdapter getConcatAdapter() {
		return concatAdapter;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = MediaCatalogFragmentBinding.inflate(inflater, container, false);

		binding.catalogCategories.setHasFixedSize(true);
		binding.catalogCategories.setAdapter(concatAdapter);

		return binding.getRoot();
	}

	private class HeaderAdapter extends SingleViewAdapter {
		private HeaderLayoutBinding binding;

		public HeaderLayoutBinding getBinding() {
			return binding;
		}

		@Nullable
		@Contract(pure = true)
		@Override
		protected View onCreateView(@NonNull ViewGroup parent) {
			var inflater = LayoutInflater.from(parent.getContext());
			this.binding = HeaderLayoutBinding.inflate(inflater, parent, false);
			setupHeader(binding);

			ViewUtil.setPadding(binding.getRoot(), ViewUtil.dpPx(16));

			ViewUtil.setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
				ViewUtil.setTopMargin(binding.getRoot(), insets.top);
				ViewUtil.setRightMargin(binding.getRoot(), insets.right);
				ViewUtil.setLeftMargin(binding.getRoot(), insets.left);
			}, parent.getRootWindowInsets());

			return binding.getRoot();
		}
	}
}