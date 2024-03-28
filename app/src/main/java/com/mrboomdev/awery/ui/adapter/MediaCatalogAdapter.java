package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.databinding.ItemGridMediaCatalogBinding;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.List;

public class MediaCatalogAdapter extends RecyclerView.Adapter<MediaCatalogAdapter.ViewHolder> {
	private static final String TAG = "MediaCatalogAdapter";
	private List<CatalogMedia> items;

	public MediaCatalogAdapter(List<CatalogMedia> items) {
		setHasStableIds(true);
		this.items = items;
	}

	public MediaCatalogAdapter() {
		this(null);
	}

	@Override
	public long getItemId(int position) {
		return Long.parseLong(items.get(position).getId("anilist"));
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(List<CatalogMedia> items) {
		if(items == null) {
			this.items = null;
			return;
		}

		this.items = items;

		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = ItemGridMediaCatalogBinding.inflate(inflater, parent, false);

		if(!ViewUtil.setRightMargin(binding.getRoot(), ViewUtil.dpPx(12))) {
			throw new IllegalStateException("Failed to set right margin!");
		}

		var viewHolder = new ViewHolder(binding);

		binding.getRoot().setOnClickListener(view ->
				MediaUtils.launchMediaActivity(parent.getContext(), viewHolder.getItem()));

		binding.getRoot().setOnLongClickListener(view -> {
			var media = viewHolder.getItem();
			var index = items.indexOf(media);

			MediaUtils.openMediaActionsMenu(parent.getContext(), media, () -> {

				if(MediaUtils.isMediaFiltered(media)) {
					items.remove(media);
					notifyItemRemoved(index);
				}
			});
			return true;
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

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemGridMediaCatalogBinding binding;
		private CatalogMedia item;

		public ViewHolder(@NonNull ItemGridMediaCatalogBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogMedia getItem() {
			return item;
		}

		public void bind(@NonNull CatalogMedia item) {
			this.item = item;

			binding.title.setText(item.getTitle());
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
				Log.e(TAG, "Failed to load a poster", e);
			}
		}
	}
}