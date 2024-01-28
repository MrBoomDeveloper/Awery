package com.mrboomdev.awery.ui.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ani.awery.databinding.MediaCatalogFeaturedBinding;

public class MediaPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final ObservableList<CatalogMedia<?>> items = new ObservableArrayList<>();
	private final PagerAdapter adapter = new PagerAdapter();
	private ViewPager2 pager;

	public MediaPagerAdapter() {
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(Collection<CatalogMedia<?>> items) {
		System.err.println(items);
		this.items.clear(false);
		this.items.addAll(items, false);
		adapter.notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void clearItems() {
		this.items.clear(false);
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		pager = new ViewPager2(parent.getContext());
		pager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
		pager.setAdapter(adapter);
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
			items.observeAdditions((item, index) -> {
				var activity = Objects.requireNonNull(AweryApp.getAnyActivity());
				activity.runOnUiThread(() -> notifyItemInserted(index));
			});
		}

		@NonNull
		@Override
		public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = MediaCatalogFeaturedBinding.inflate(inflater, parent, false);

			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
			);

			binding.getRoot().setLayoutParams(params);
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

		public void bind(@NonNull CatalogMedia<?> item) {
			binding.title.setText(item.title);
			binding.description.setText(Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT));

			var tagsCount = new AtomicInteger(0);
			binding.tags.setText(item.genres.stream()
					.filter(tag -> tagsCount.getAndAdd(1) < 3)
					.collect(Collectors.joining(", ")));

			Glide.with(binding.getRoot())
					.load(item.poster.extraLarge)
					.transition(withCrossFade())
					.into(binding.poster);

			if(item.banner != null) {

				/*
				  Because of some strange bug we have to load banner by our hands
				  or else it'll in some moment will stretch
				*/

				Glide.with(binding.getRoot())
						.load(item.banner)
						.into(new CustomTarget<Drawable>() {
							@Override
							public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
								item.cachedBanner = resource;
								binding.banner.setImageAlpha(0);
								binding.banner.setImageDrawable(resource);
								TransitionManager.beginDelayedTransition(binding.posterHolder);
								binding.banner.setImageAlpha(255);
							}

							@Override
							public void onLoadCleared(@Nullable Drawable placeholder) {
								item.cachedBanner = null;
							}
						});
			} else {
				Glide.with(binding.getRoot()).clear(binding.banner);
				binding.banner.setImageDrawable(null);
			}
		}
	}
}