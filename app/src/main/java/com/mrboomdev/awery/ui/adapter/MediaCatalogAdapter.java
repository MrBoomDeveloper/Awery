package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ani.awery.databinding.MediaCatalogItemBinding;

public class MediaCatalogAdapter extends RecyclerView.Adapter<MediaCatalogAdapter.ViewHolder> {
	private final Map<Integer, ViewHolder> cachedItems = new HashMap<>();
	private ObservableList<CatalogMedia<?>> items;
	private ClickCallback clickCallback;

	public MediaCatalogAdapter(ObservableList<CatalogMedia<?>> items) {
		setHasStableIds(true);
		this.items = items;

		setOnClickListener(media -> {
			System.out.println("clicked: " + media);
		});
	}

	public MediaCatalogAdapter() {
		this(null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setOnClickListener(ClickCallback listener) {
		this.clickCallback = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(@NonNull ObservableList<CatalogMedia<?>> items) {
		boolean shouldResetList = (this.items == null)
				|| (this.items.size() != items.size());

		this.items = items;

		if(shouldResetList) {
			notifyDataSetChanged();
		} else {
			for(var entry : cachedItems.entrySet()) {
				var index = entry.getKey();
				var viewHolder = entry.getValue();
				var item = items.get(index);
				viewHolder.bind(item);
			}
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = MediaCatalogItemBinding.inflate(inflater, parent, false);
		var viewHolder = new ViewHolder(binding);

		binding.getRoot().setOnClickListener(view -> {
			if(clickCallback == null) return;
			clickCallback.clicked(viewHolder.getItem());
		});

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(items.get(position));
		cachedItems.put(position, holder);
	}

	@Override
	public int getItemCount() {
		if(items == null) {
			return 0;
		}

		return items.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final MediaCatalogItemBinding binding;
		private CatalogMedia<?> item;

		public ViewHolder(@NonNull MediaCatalogItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogMedia<?> getItem() {
			return item;
		}

		public void bind(@NonNull CatalogMedia<?> item) {
			this.item = item;
			binding.title.setText(item.title);

			Glide.with(binding.getRoot())
					.load(item.poster.medium)
					.into(binding.mediaItemBanner);

			AweryApp.setTimeout(() -> {
				Glide.with(binding.getRoot())
						.load(item.poster.large)
						.into(binding.mediaItemBanner);
			}, 5000);

			AweryApp.setTimeout(() -> {
				Glide.with(binding.getRoot())
						.load(item.poster.extraLarge)
						.into(binding.mediaItemBanner);
			}, 10000);
		}
	}

	public interface ClickCallback {
		void clicked(CatalogMedia<?> media);
	}
}