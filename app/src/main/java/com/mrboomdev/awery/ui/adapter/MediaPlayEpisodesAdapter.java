package com.mrboomdev.awery.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrboomdev.awery.catalog.template.CatalogEpisode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import ani.awery.databinding.ItemListEpisodeBinding;

public class MediaPlayEpisodesAdapter extends RecyclerView.Adapter<MediaPlayEpisodesAdapter.ViewHolder> {
	private OnEpisodeSelectedListener onEpisodeSelectedListener;
	private ArrayList<CatalogEpisode> items = new ArrayList<>();

	public MediaPlayEpisodesAdapter() {
		setHasStableIds(true);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(@NonNull Collection<CatalogEpisode> items) {
		this.items = new ArrayList<>(items);
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
		return position;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = ItemListEpisodeBinding.inflate(inflater, parent, false);
		var holder = new ViewHolder(binding);

		binding.container.setOnClickListener(v -> {
			if(onEpisodeSelectedListener == null) return;
			onEpisodeSelectedListener.onEpisodeSelected(holder.getItem(), items);
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

	public static class ViewHolder extends RecyclerView.ViewHolder {
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

			var calendar = Calendar.getInstance();
			calendar.setTimeInMillis(item.getReleaseDate());

			binding.title.setText(item.getTitle());

			binding.description.setText(calendar.get(Calendar.DATE)
					+ "/" + (calendar.get(Calendar.MONTH) + 1)
					+ "/" + calendar.get(Calendar.YEAR));

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