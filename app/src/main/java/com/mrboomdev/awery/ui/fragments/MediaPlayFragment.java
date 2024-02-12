package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.List;

import ani.awery.R;
import ani.awery.databinding.MediaDetailsWatchVariantsBinding;

public class MediaPlayFragment extends Fragment {
	private final ConcatAdapter concatAdapter;
	private SingleViewAdapter headerAdapter;

	public MediaPlayFragment() {
		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		concatAdapter = new ConcatAdapter(config);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		var layoutManager = new FlexboxLayoutManager(inflater.getContext());

		var headerBinding = MediaDetailsWatchVariantsBinding.inflate(inflater, container, false);
		headerAdapter = SingleViewAdapter.fromView(headerBinding.getRoot());
		concatAdapter.addAdapter(headerAdapter);

		headerBinding.sourceDropdown.setAdapter(new ArrayAdapter<>(inflater.getContext(),
				R.layout.menu_dropdown_item, List.of("Kodik", "HAnime", "Anilibria", "Onwave")));

		headerBinding.seasonDropdown.setAdapter(new ArrayAdapter<>(inflater.getContext(),
				R.layout.menu_dropdown_item, List.of("1", "2", "3", "4")));

		headerBinding.variantDropdown.setAdapter(new ArrayAdapter<>(inflater.getContext(),
				R.layout.menu_dropdown_item, List.of("Anilibria", "AniDub", "StudioBand", "Onwave")));

		ViewUtil.setOnApplyUiInsetsListener(headerBinding.getRoot(), insets -> {
			ViewUtil.setTopPadding(headerBinding.getRoot(), insets.top);
			ViewUtil.setRightPadding(headerBinding.getRoot(), insets.right);
		}, container);

		var recycler = new RecyclerView(inflater.getContext());
		recycler.setLayoutManager(layoutManager);
		recycler.setAdapter(concatAdapter);
		return recycler;
	}
}