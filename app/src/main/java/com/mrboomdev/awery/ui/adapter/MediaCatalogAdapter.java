package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ani.awery.databinding.MediaCatalogItemBinding;

public class MediaCatalogAdapter extends RecyclerView.Adapter<MediaCatalogAdapter.ViewHolder> {
	private final Map<Integer, ViewHolder> cachedItems = new HashMap<>();
	private ObservableList<String> items;
	private ClickCallback clickCallback;

	public MediaCatalogAdapter(ObservableList<String> items) {
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
	public void setItems(@NonNull ObservableList<String> items) {
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
		private String item;

		public ViewHolder(@NonNull MediaCatalogItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public String getItem() {
			return item;
		}

		public void bind(@NonNull String item) {
			this.item = item;
			binding.title.setText(item);

			Glide.with(binding.getRoot())
					.load("https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx142984-nv2MWVWZ1yYH.jpg")
					.into(binding.mediaItemBanner);
		}
	}

	public interface ClickCallback {
		void clicked(String media);
	}
}