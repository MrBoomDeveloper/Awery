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
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import ani.awery.databinding.LayoutHeaderBinding;
import ani.awery.databinding.LayoutLoadingBinding;
import ani.awery.databinding.MediaCatalogFragmentBinding;
import ani.awery.media.SearchActivity;

public class MediaCatalogFragment extends Fragment {
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutLoadingBinding> emptyAdapter;
	private SingleViewAdapter.BindingSingleViewAdapter<LayoutHeaderBinding> headerAdapter;
	private final ConcatAdapter concatAdapter;
	private MediaCatalogFragmentBinding binding;

	public MediaCatalogFragment() {
		var config = new ConcatAdapter.Config.Builder()
				.setIsolateViewTypes(true)
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		concatAdapter = new ConcatAdapter(config);
	}

	public void setupHeader(@NonNull LayoutHeaderBinding header) {
		if(header == null) {
			throw new IllegalArgumentException("Header cannot be null");
		}

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

	public SingleViewAdapter.BindingSingleViewAdapter<LayoutLoadingBinding> getEmptyAdapter() {
		return emptyAdapter;
	}

	public SingleViewAdapter.BindingSingleViewAdapter<LayoutHeaderBinding> getHeaderAdapter() {
		return headerAdapter;
	}

	public void setEmptyData(boolean isLoading, String title, String message) {
		getEmptyAdapter().getBinding((binding, didJustCreated) -> {
			binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

			if(title != null || message != null) {
				binding.info.setVisibility(View.VISIBLE);
				binding.title.setText(title);
				binding.message.setText(message);
			} else {
				binding.info.setVisibility(View.GONE);
			}
		});
	}

	public void setEmptyData(boolean isLoading) {
		setEmptyData(isLoading, null, null);
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

		var headerBinding = LayoutHeaderBinding.inflate(inflater, container, false);
		setupHeader(headerBinding);

		ViewUtil.setPadding(headerBinding.getRoot(), ViewUtil.dpPx(16));

		ViewUtil.setOnApplyUiInsetsListener(headerBinding.getRoot(), insets -> {
			ViewUtil.setTopMargin(headerBinding.getRoot(), insets.top);
			ViewUtil.setRightMargin(headerBinding.getRoot(), insets.right);
			ViewUtil.setLeftMargin(headerBinding.getRoot(), insets.left);
		}, container);

		var headerParams = ViewUtil.createLinearParams(ViewUtil.MATCH_PARENT, ViewUtil.WRAP_CONTENT);
		headerAdapter = SingleViewAdapter.fromBinding(headerBinding, headerParams);

		var emptyBinding = LayoutLoadingBinding.inflate(inflater);
		var emptyParams = ViewUtil.createLinearParams(ViewUtil.MATCH_PARENT, ViewUtil.WRAP_CONTENT);
		ViewUtil.setVerticalMargin(emptyParams, ViewUtil.dpPx(64));
		ViewUtil.setHorizontalMargin(emptyParams, ViewUtil.dpPx(16));
		emptyAdapter = SingleViewAdapter.fromBinding(emptyBinding, emptyParams);

		return binding.getRoot();
	}
}