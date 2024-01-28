package com.mrboomdev.awery.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.mrboomdev.awery.util.ContextUtil;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;

import ani.awery.R;
import ani.awery.databinding.MediaCatalogFeaturedBinding;

public class MediaPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final ObservableList<String> items = new ObservableArrayList<>();
	private ViewPager2 pager;

	public MediaPagerAdapter() {
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(String item, boolean notify) {
		items.add(item, notify);
	}

	public void addItem(String item) {
		items.add(item);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ContextUtil.dpPx(300));
		pager = new ViewPager2(parent.getContext());
		pager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
		pager.setLayoutParams(params);
		pager.setAdapter(new PagerAdapter());
		return new RecyclerView.ViewHolder(pager) {};
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

	@Override
	public int getItemCount() {
		return 1;
	}

	public class PagerAdapter extends RecyclerView.Adapter<PagerViewHolder> {

		public PagerAdapter() {
			items.observeAdditions((item, index) -> notifyItemInserted(index));
		}

		@NonNull
		@Override
		public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = MediaCatalogFeaturedBinding.inflate(inflater, parent, false);
			return new PagerViewHolder(binding);
		}

		@Override
		public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			return items.size();
		}
	}

	public static class PagerViewHolder extends RecyclerView.ViewHolder {
		private final MediaCatalogFeaturedBinding binding;

		public PagerViewHolder(@NonNull MediaCatalogFeaturedBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bind(String item) {
			binding.title.setText(item);
		}
	}
}