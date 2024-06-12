package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setPadding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mrboomdev.awery.databinding.LayoutHeaderMainBinding;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.ui.activity.SearchActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.rubensousa.dpadrecyclerview.DpadRecyclerView;

import java.util.List;
import java.util.Objects;

public class FeedsFragment extends Fragment {
	private List<CatalogFeed> feeds;

	@Override
	@SuppressWarnings("unchecked")
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.feeds = (List<CatalogFeed>) requireArguments().getSerializable("feeds");
	}

	@Nullable
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
		var frame = new FrameLayout(requireContext());

		var refresher = new SwipeRefreshLayout(requireContext());
		frame.addView(refresher, MATCH_PARENT, MATCH_PARENT);

		var recycler = new DpadRecyclerView(requireContext());
		refresher.addView(recycler, MATCH_PARENT, MATCH_PARENT);

		var header = LayoutHeaderMainBinding.inflate(getLayoutInflater());
		frame.addView(header.getRoot(), MATCH_PARENT, WRAP_CONTENT);
		setupHeader(header, recycler);

		refresher.setOnRefreshListener(() -> refresher.setRefreshing(false));

		return frame;
	}

	private void setupHeader(@NonNull LayoutHeaderMainBinding header, RecyclerView recycler) {
		header.search.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SearchActivity.class);
			startActivity(intent);
		});

		header.settingsWrapper.setOnClickListener(v -> {
			var intent = new Intent(requireActivity(), SettingsActivity.class);
			startActivity(intent);
		});

		setPadding(header.getRoot(), ViewUtil.dpPx(16));

		setOnApplyUiInsetsListener(header.getRoot(), insets -> {
			ViewUtil.setTopMargin(header.getRoot(), insets.top);
			ViewUtil.setRightMargin(header.getRoot(), insets.right);
			ViewUtil.setLeftMargin(header.getRoot(), insets.left);
			return false;
		});
	}
}