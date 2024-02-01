package com.mrboomdev.awery.ui.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;
import com.mrboomdev.awery.util.ViewUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ani.awery.databinding.MediaCatalogFeaturedBinding;
import ani.awery.databinding.MediaCatalogFeaturedPagerBinding;

public class MediaPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final ObservableList<CatalogMedia<?>> items = new ObservableArrayList<>();
	private View root;
	private int visibility = View.VISIBLE;
	private final PagerAdapter adapter = new PagerAdapter();
	private ProgressBar progressBar;
	private ViewPager2 pager;
	private boolean isLoading;

	public MediaPagerAdapter() {
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setVisibility(int visibility) {
		if(this.visibility == View.VISIBLE && visibility == View.GONE) {
			notifyItemRemoved(0);
		}

		if(this.visibility == View.GONE && visibility == View.VISIBLE) {
			notifyItemInserted(0);
		}

		this.visibility = visibility;
	}

	public void setIsLoading(boolean isLoading) {
		this.isLoading = isLoading;

		if(progressBar != null) {
			progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(Collection<CatalogMedia<?>> items) {
		this.items.clear(false);
		this.items.addAll(items, false);
		adapter.notifyDataSetChanged();
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = MediaCatalogFeaturedPagerBinding.inflate(inflater, parent, false);

		pager = binding.pager;
		pager.setAdapter(adapter);

		progressBar = binding.progress;
		progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

		root = binding.getRoot();
		setVisibility(visibility);

		return new RecyclerView.ViewHolder(binding.getRoot()) {};
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

			ViewUtil.setOnApplyUiInsetsListener(binding.leftSideBarrier, (view, insets) ->
					ViewUtil.setLeftMargin(view, insets.left), parent.getRootWindowInsets());

			ViewUtil.setOnApplyUiInsetsListener(binding.rightSideBarrier, (view, insets) ->
					ViewUtil.setRightMargin(view, insets.right), parent.getRootWindowInsets());

			ViewUtil.setOnApplyUiInsetsListener(binding.topSideBarrier, (view, insets) ->
					ViewUtil.setTopMargin(view, insets.top), parent.getRootWindowInsets());

			return new PagerViewHolder(binding);
		}

		@Override
		public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			if(visibility == View.GONE) {
				return 0;
			}

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
								binding.banner.setImageDrawable(resource);
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