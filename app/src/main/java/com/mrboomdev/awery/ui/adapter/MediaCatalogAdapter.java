package com.mrboomdev.awery.ui.adapter;

import static com.mrboomdev.awery.util.MediaUtils.launchMediaActivity;
import static com.mrboomdev.awery.util.MediaUtils.openMediaActionsMenu;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrboomdev.awery.databinding.GridMediaCatalogBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public class MediaCatalogAdapter extends RecyclerView.Adapter<MediaCatalogAdapter.ViewHolder> {
	private static final String TAG = "MediaCatalogAdapter";
	private final WeakHashMap<CatalogMedia, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private List<? extends CatalogMedia> items;

	public MediaCatalogAdapter(List<CatalogMedia> items) {
		setHasStableIds(true);
		this.items = items;

		if(items != null) {
			for(var item : items) {
				ids.put(item, idGenerator.getLong());
			}
		}
	}

	public MediaCatalogAdapter() {
		this(null);
	}

	@Override
	public long getItemId(int position) {
		var id = ids.get(items.get(position));

		if(id == null) {
			throw new IllegalStateException("No id found for item at position " + position);
		}

		return id;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(List<? extends CatalogMedia> items) {
		idGenerator.clear();

		if(items == null) {
			this.items = null;
			return;
		} else {
			for(var item : items) {
				ids.put(item, idGenerator.getLong());
			}
		}

		this.items = items;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = GridMediaCatalogBinding.inflate(inflater, parent, false);

		if(!setRightMargin(binding.getRoot(), dpPx(12))) {
			throw new IllegalStateException("Failed to set right margin!");
		}

		var viewHolder = new ViewHolder(binding);

		binding.getRoot().setOnClickListener(view ->
				launchMediaActivity(parent.getContext(), viewHolder.getItem()));

		binding.getRoot().setOnLongClickListener(view -> {
			var media = viewHolder.getItem();
			var index = items.indexOf(media);

			openMediaActionsMenu(parent.getContext(), media, () -> MediaUtils.isMediaFiltered(media, isFiltered -> {
				if(!isFiltered) return;

				items.remove(media);
				notifyItemRemoved(index);
			}));
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
		return items != null ? items.size() : 0;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final GridMediaCatalogBinding binding;
		private CatalogMedia item;

		public ViewHolder(@NonNull GridMediaCatalogBinding binding) {
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