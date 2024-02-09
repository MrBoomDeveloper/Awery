package com.mrboomdev.awery.ui.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogCategory;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ani.awery.R;
import ani.awery.databinding.MediaCatalogFeaturedBinding;
import ani.awery.databinding.MediaCatalogFeaturedPagerBinding;

public class MediaPagerAdapter extends SingleViewAdapter {
	private final ObservableList<CatalogMedia<?>> items = new ObservableArrayList<>();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private View headerView;
	private LinearLayout headerLayout;
	private final PagerAdapter adapter = new PagerAdapter();
	private ProgressBar progressBar;
	private boolean isLoading;

	private void attachHeaderView(View view) {
		headerLayout.removeAllViews();
		headerLayout.addView(view, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

		handler.post(() -> notifyItemRangeChanged(0, items.size()));

		if(Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			var logo = view.findViewById(R.id.logo);
			logo.setVisibility(View.GONE);
		}

		ViewUtil.setOnApplyUiInsetsListener(view, (v, insets) -> {
			ViewUtil.setTopMargin(v, insets.top);
			ViewUtil.setRightMargin(v, insets.right);
			ViewUtil.setLeftPadding(v, insets.left);
		}, headerLayout.getRootWindowInsets());
	}

	public void setHeaderView(View view) {
		if(view == null && this.headerView != null) {
			headerLayout.removeView(this.headerView);
		}

		this.headerView = view;

		if(headerLayout != null && view != null) {
			attachHeaderView(view);
		}
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
	public View onCreateView(@NonNull ViewGroup parent) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = MediaCatalogFeaturedPagerBinding.inflate(inflater, parent, false);

		ViewPager2 pager = binding.pager;
		pager.setAdapter(adapter);

		progressBar = binding.progress;
		progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

		headerLayout = binding.header;
		setEnabled(isEnabled());

		if(headerView != null) {
			attachHeaderView(headerView);
		}

		return binding.getRoot();
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
			var holder = new PagerViewHolder(binding);

			View.OnClickListener clickListener = v -> {
				var item = holder.getItem();
				if(item != null) item.handleClick(parent.getContext());
			};

			Runnable longClickListener = () -> {
				var item = holder.getItem();
				if(item != null) item.handleLongClick(parent.getContext());
			};

			binding.getRoot().setOnClickListener(clickListener);
			binding.watch.setOnClickListener(clickListener);
			binding.bookmark.setOnClickListener(v -> longClickListener.run());

			binding.getRoot().setOnLongClickListener(v -> {
				longClickListener.run();
				return true;
			});

			ViewUtil.setOnApplyUiInsetsListener(binding.leftSideBarrier, (view, insets) ->
					ViewUtil.setLeftMargin(view, insets.left), parent.getRootWindowInsets());

			ViewUtil.setOnApplyUiInsetsListener(binding.rightSideBarrier, (view, insets) ->
					ViewUtil.setRightMargin(view, insets.right), parent.getRootWindowInsets());

			ViewUtil.setOnApplyUiInsetsListener(binding.topSideBarrier, (view, insets) ->
					ViewUtil.setTopMargin(view, insets.top), parent.getRootWindowInsets());

			return holder;
		}

		@Override
		public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			if(!isEnabled()) {
				return 0;
			}

			return items.size();
		}
	}

	public class PagerViewHolder extends RecyclerView.ViewHolder {
		private final MediaCatalogFeaturedBinding binding;
		private CatalogMedia<?> item;

		public PagerViewHolder(@NonNull MediaCatalogFeaturedBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogMedia<?> getItem() {
			return item;
		}

		public void bind(@NonNull CatalogMedia<?> item) {
			this.item = item;

			binding.title.setText(item.title);
			binding.description.setText(Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT));

			var tagsCount = new AtomicInteger(0);
			var formattedTags = item.genres != null ? (
					item.genres.stream()
							.filter(tag -> tagsCount.getAndAdd(1) < 3)
							.collect(Collectors.joining(", "))
					) : (
							item.tags.stream()
									.filter(tag -> tagsCount.getAndAdd(1) < 3)
									.map(tag -> tag.name)
									.collect(Collectors.joining(", ")));

			binding.tags.setText(formattedTags);

			binding.poster.setImageDrawable(null);
			binding.banner.setImageDrawable(null);

			Glide.with(binding.getRoot())
					.load(item.poster.extraLarge)
					.transition(withCrossFade())
					.into(binding.poster);

			/*
			 Because of some strange bug we have to load banner by our hands
			 or else it'll in some moment will stretch
			*/

			Glide.with(binding.getRoot())
					.load(item.banner != null ? item.banner : item.poster.extraLarge)
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

			if(binding.headerBarrier != null) {
				binding.headerBarrier.setVisibility(headerView != null ? View.VISIBLE : View.GONE);
			}
		}
	}
}