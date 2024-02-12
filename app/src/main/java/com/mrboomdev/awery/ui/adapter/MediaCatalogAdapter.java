package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ObservableList;

import java.util.HashMap;
import java.util.Map;

import ani.awery.databinding.MediaCatalogItemBinding;
import ani.awery.media.MediaDetailsActivity;

public class MediaCatalogAdapter extends RecyclerView.Adapter<MediaCatalogAdapter.ViewHolder> implements ObservableList.AddObserver<CatalogMedia> {
	private ObservableList<CatalogMedia> items;

	public MediaCatalogAdapter(ObservableList<CatalogMedia> items) {
		setHasStableIds(true);
		this.items = items;
	}

	public MediaCatalogAdapter() {
		this(null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(ObservableList<CatalogMedia> items) {
		if(this.items != null) {
			this.items.removeAdditionObserver(this);
		}

		if(items == null) {
			this.items = null;
			return;
		}

		this.items = items;
		this.items.observeAdditions(this);

		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = MediaCatalogItemBinding.inflate(inflater, parent, false);
		var viewHolder = new ViewHolder(binding);

		binding.getRoot().setOnClickListener(view -> {
			var item = viewHolder.getItem();
			if(item != null) item.handleClick(parent.getContext());
		});

		binding.getRoot().setOnLongClickListener(view -> {
			var item = viewHolder.getItem();

			if(item != null) {
				item.handleLongClick(parent.getContext());
				return true;
			}

			return false;
		});

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(items.get(position));
	}

	@Override
	public int getItemCount() {
		if(items == null) {
			return 0;
		}

		return items.size();
	}

	@Override
	public void added(CatalogMedia item, int index) {
		notifyItemInserted(index);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final MediaCatalogItemBinding binding;
		private CatalogMedia item;

		public ViewHolder(@NonNull MediaCatalogItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogMedia getItem() {
			return item;
		}

		public void bind(@NonNull CatalogMedia item) {
			this.item = item;

			binding.title.setText(item.title);
			binding.ongoing.setVisibility(item.status == CatalogMedia.MediaStatus.ONGOING ? View.VISIBLE : View.GONE);

			if(item.averageScore != null) {
				binding.scoreWrapper.setVisibility(View.VISIBLE);
				binding.score.setText(String.valueOf(item.averageScore));
			} else {
				binding.scoreWrapper.setVisibility(View.GONE);
			}

			try {
				Glide.with(binding.getRoot())
						.load(item.poster.large)
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(binding.mediaItemBanner);
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
}