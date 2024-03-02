package com.mrboomdev.awery.ui.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.catalog.template.CatalogTag;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.ObservableArrayList;
import com.mrboomdev.awery.util.ObservableList;
import com.mrboomdev.awery.util.ui.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ani.awery.databinding.LayoutHeaderBinding;
import ani.awery.databinding.MediaCatalogFeaturedBinding;
import ani.awery.databinding.MediaCatalogFeaturedPagerBinding;

public class MediaPagerAdapter extends SingleViewAdapter {
	private final ObservableList<CatalogMedia> items = new ObservableArrayList<>();
	private LayoutHeaderBinding header;
	private final PagerAdapter adapter = new PagerAdapter();

	public LayoutHeaderBinding getHeader() {
		return header;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(Collection<CatalogMedia> items) {
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

		this.header = binding.header;
		setEnabled(isEnabled());

		if(Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			binding.header.logo.setVisibility(View.GONE);
		}

		ViewUtil.setOnApplyUiInsetsListener(binding.headerWrapper, insets -> {
			ViewUtil.setTopMargin(binding.headerWrapper, insets.top);
			ViewUtil.setRightMargin(binding.headerWrapper, insets.right);
			ViewUtil.setLeftMargin(binding.headerWrapper, insets.left);
		}, parent);

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

			binding.getRoot().setOnClickListener(v -> MediaUtils.launchMediaActivity(parent.getContext(), holder.getItem()));
			binding.watch.setOnClickListener(v -> MediaUtils.launchMediaActivity(parent.getContext(), holder.getItem(), "watch"));
			binding.bookmark.setOnClickListener(v -> MediaUtils.openMediaBookmarkMenu(parent.getContext(), holder.getItem()));

			binding.getRoot().setOnLongClickListener(v -> {
				MediaUtils.openMediaActionsMenu(parent.getContext(), holder.getItem());
				return true;
			});

			ViewUtil.setOnApplyUiInsetsListener(binding.leftSideBarrier, insets ->
					ViewUtil.setLeftMargin(binding.leftSideBarrier, insets.left), parent.getRootWindowInsets());

			ViewUtil.setOnApplyUiInsetsListener(binding.rightSideBarrier, insets ->
					ViewUtil.setRightMargin(binding.rightSideBarrier, insets.right), parent.getRootWindowInsets());

			ViewUtil.setOnApplyUiInsetsListener(binding.topSideBarrier, insets ->
					ViewUtil.setTopMargin(binding.topSideBarrier, insets.top), parent.getRootWindowInsets());

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
		private CatalogMedia item;

		public PagerViewHolder(@NonNull MediaCatalogFeaturedBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogMedia getItem() {
			return item;
		}

		@SuppressLint("SetTextI18n")
		public void bind(@NonNull CatalogMedia item) {
			this.item = item;

			binding.title.setText(item.title);
			binding.description.setText(Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT));

			if(item.averageScore != null) {
				binding.metaSeparator.setVisibility(View.VISIBLE);
				binding.status.setVisibility(View.VISIBLE);
				binding.status.setText(item.averageScore + "/10");
			} else {
				binding.metaSeparator.setVisibility(View.GONE);
				binding.status.setVisibility(View.GONE);
			}

			var tagsCount = new AtomicInteger(0);
			var formattedTags = item.genres != null ? (
					item.genres.stream()
							.filter(tag -> tagsCount.getAndAdd(1) < 3)
							.collect(Collectors.joining(", "))
					) : (
							item.tags.stream()
									.filter(tag -> tagsCount.getAndAdd(1) < 3)
									.map(CatalogTag::getName)
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
		}
	}
}