package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrboomdev.awery.databinding.ItemListEpisodeBinding;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.util.UniqueIdGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class MediaPlayEpisodesAdapter extends RecyclerView.Adapter<MediaPlayEpisodesAdapter.ViewHolder> {
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private OnEpisodeSelectedListener onEpisodeSelectedListener;
	private ArrayList<CatalogEpisode> items = new ArrayList<>();
	private CatalogMedia media;

	public MediaPlayEpisodesAdapter() {
		setHasStableIds(true);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(CatalogMedia media, @NonNull Collection<? extends CatalogEpisode> items) {
		this.media = media;
		idGenerator.clear();

		for(var item : items) {
			item.setId(idGenerator.getLong());
		}

		this.items = new ArrayList<>(items);
		Collections.sort(this.items);

		notifyDataSetChanged();
	}

	public void setOnEpisodeSelectedListener(OnEpisodeSelectedListener listener) {
		this.onEpisodeSelectedListener = listener;
	}

	public interface OnEpisodeSelectedListener {
		void onEpisodeSelected(@NonNull CatalogEpisode episode, ArrayList<CatalogEpisode> episodes);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).getId();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = ItemListEpisodeBinding.inflate(inflater, parent, false);
		var holder = new ViewHolder(binding);

		binding.container.setOnClickListener(v -> {
			var item = holder.getItem();

			/*if(media.lastEpisode < item.getNumber()) {
				media.lastEpisode = item.getNumber();
				notifyItemRangeChanged(0, items.size());

				new Thread(() -> {
					var db = AweryApp.getDatabase().getMediaDao();
					var dbMedia = DBCatalogMedia.fromCatalogMedia(media);
					db.insert(dbMedia);
				}).start();
			}*/
			
			if(onEpisodeSelectedListener == null) return;
			onEpisodeSelectedListener.onEpisodeSelected(item, items);
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(items.get(position));
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemListEpisodeBinding binding;
		private CatalogEpisode item;

		public ViewHolder(@NonNull ItemListEpisodeBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogEpisode getItem() {
			return item;
		}

		@SuppressLint("SetTextI18n")
		public void bind(@NonNull CatalogEpisode item) {
			this.item = item;

			binding.title.setText(item.getTitle());
			//binding.container.setAlpha((media.lastEpisode >= item.getNumber()) ? .5f : 1);

			if(item.getReleaseDate() > 0) {
				var calendar = Calendar.getInstance();
				calendar.setTimeInMillis(item.getReleaseDate());

				binding.description.setVisibility(View.VISIBLE);

				binding.description.setText(calendar.get(Calendar.DATE)
						+ "/" + (calendar.get(Calendar.MONTH) + 1)
						+ "/" + calendar.get(Calendar.YEAR));
			} else {
				binding.description.setVisibility(View.GONE);
			}

			if(item.getBanner() != null) {
				binding.banner.setVisibility(View.VISIBLE);
				binding.banner.setImageDrawable(null);

				Glide.with(binding.banner)
						.load(item.getBanner())
						.into(binding.banner);
			} else {
				binding.banner.setVisibility(View.GONE);
				Glide.with(binding.banner).clear(binding.banner);
			}
		}
	}
}