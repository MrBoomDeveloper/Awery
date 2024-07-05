package com.mrboomdev.awery.ui.fragments.feeds;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.mrboomdev.awery.app.AweryApp.getConfiguration;
import static com.mrboomdev.awery.app.AweryApp.getNavigationStyle;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.requireNonNull;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mrboomdev.awery.databinding.MediaCatalogFeaturedBinding;
import com.mrboomdev.awery.databinding.MediaCatalogFeaturedPagerBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.MediaUtils;

import org.jetbrains.annotations.Contract;

import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import java9.util.stream.Collectors;

public class PagesFeedViewHolder extends FeedViewHolder {
	private static final int MAX_ITEMS = 10;
	private final WeakHashMap<CatalogMedia, Long> ids = new WeakHashMap<>();
	private final PagerAdapter adapter = new PagerAdapter();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private Feed feed;

	@NonNull
	@Contract("_ -> new")
	public static PagesFeedViewHolder create(ViewGroup parent) {
		return new PagesFeedViewHolder(MediaCatalogFeaturedPagerBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent,false), parent);
	}

	private PagesFeedViewHolder(@NonNull MediaCatalogFeaturedPagerBinding binding, ViewGroup parent) {
		super(binding.getRoot());
		binding.pager.setAdapter(adapter);

		setOnApplyUiInsetsListener(binding.pageIndicator, insets -> {
			if(isLandscape()) {
				setRightMargin(binding.pageIndicator, insets.right + dpPx(16));
			} else {
				setRightMargin(binding.pageIndicator, 0);
			}

			return true;
		}, parent);
	}

	@SuppressLint("NotifyDataSetChanged")
	@Override
	public void bind(@NonNull Feed feed) {
		this.feed = feed;
		idGenerator.clear();

		if(feed.getItems() != null) {
			for(var item : feed.getItems()) {
				ids.put(item, idGenerator.getLong());
			}
		}

		adapter.notifyDataSetChanged();
	}

	private class PagerAdapter extends RecyclerView.Adapter<PagerViewHolder> {

		{ setHasStableIds(true); }

		@NonNull
		@Override
		public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = MediaCatalogFeaturedBinding.inflate(inflater, parent, false);
			var holder = new PagerViewHolder(binding);

			binding.getRoot().setOnClickListener(v -> MediaUtils.launchMediaActivity(
					parent.getContext(), holder.getItem()));

			binding.watch.setOnClickListener(v -> MediaUtils.launchMediaActivity(
					parent.getContext(), holder.getItem(), "watch"));

			binding.bookmark.setOnClickListener(v -> MediaUtils.openMediaBookmarkMenu(
					parent.getContext(), holder.getItem()));

			binding.getRoot().setOnLongClickListener(v -> {
				var media = holder.getItem();
				var index = feed.getItems().indexOf(media);

				MediaUtils.openMediaActionsMenu(parent.getContext(), media, () -> {
					MediaUtils.isMediaFiltered(media, isFiltered -> {
						if(!isFiltered) return;

						runOnUiThread(() -> {
							var was = feed.getItems().remove(index);
							notifyItemRemoved(index);
							ids.remove(was);
						});
					});
				});

				return true;
			});

			setOnApplyUiInsetsListener(binding.leftSideBarrier, insets -> {
				if(isLandscape()) {
					setLeftMargin(binding.leftSideBarrier, dpPx(32) +
							(getNavigationStyle() != AwerySettings.NavigationStyle_Values.MATERIAL ? insets.left : 0));
				} else {
					setRightMargin(binding.leftSideBarrier, 0);
				}

				return true;
			}, parent);

			setOnApplyUiInsetsListener(binding.rightSideBarrier, insets -> {
				setRightMargin(binding.rightSideBarrier, insets.right);
				return true;
			}, parent);

			setOnApplyUiInsetsListener(binding.topSideBarrier, insets -> {
				setTopMargin(binding.topSideBarrier, insets.top);
				return true;
			}, parent);

			return holder;
		}

		@Override
		public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
			holder.bind(feed.getItems().get(position));
		}

		@Override
		public long getItemId(int position) {
			return requireNonNull(ids.get(feed.getItems().get(position)));
		}

		@Override
		public int getItemCount() {
			if(feed == null) {
				return 0;
			}

			return Math.min(feed.getItems().size(), MAX_ITEMS);
		}
	}

	private static class PagerViewHolder extends RecyclerView.ViewHolder {
		private final MediaCatalogFeaturedBinding binding;
		private CatalogMedia item;

		public PagerViewHolder(@NonNull MediaCatalogFeaturedBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		@NonNull
		public View getView() {
			return binding.getRoot();
		}

		public CatalogMedia getItem() {
			return item;
		}

		@SuppressLint("SetTextI18n")
		public void bind(@NonNull CatalogMedia item) {
			binding.title.setText(item.getTitle());

			var description = item.description == null ? null :
					Html.fromHtml(item.description, Html.FROM_HTML_MODE_COMPACT).toString().trim();

			while(description != null && description.contains("\n\n")) {
				description = description.replaceAll("\n\n", "\n");
			}

			binding.description.setText(description);

			if(item.averageScore != null) {
				binding.metaSeparator.setVisibility(View.VISIBLE);
				binding.status.setVisibility(View.VISIBLE);
				binding.status.setText(item.averageScore + "/10");
			} else {
				binding.metaSeparator.setVisibility(View.GONE);
				binding.status.setVisibility(View.GONE);
			}

			var tagsCount = new AtomicInteger(0);

			var formattedTags = (item.genres != null ? (
					stream(item.genres)
							.filter(tag -> tagsCount.getAndAdd(1) < 3)
			) : (
					stream(item.tags)
							.filter(tag -> tagsCount.getAndAdd(1) < 3)
							.map(CatalogTag::getName)
			)).collect(Collectors.joining(", "));

			binding.tags.setText(formattedTags);

			binding.poster.setImageDrawable(null);
			binding.banner.setImageDrawable(null);

			if((getConfiguration(getContext(binding)).uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
				binding.metaSeparator.setShadowLayer(1, 0, 0, Color.BLACK);
				binding.tags.setShadowLayer(1, 0, 0, Color.BLACK);
				binding.status.setShadowLayer(1, 0, 0, Color.BLACK);
				binding.title.setShadowLayer(3, 0, 0, Color.BLACK);
				binding.description.setShadowLayer(2, 0, 0, Color.BLACK);
			} else {
				binding.metaSeparator.setShadowLayer(0, 0, 0, 0);
				binding.tags.setShadowLayer(0, 0, 0, 0);
				binding.status.setShadowLayer(0, 0, 0, 0);
				binding.title.setShadowLayer(0, 0, 0, 0);
				binding.description.setShadowLayer(0, 0, 0, 0);
			}

			Glide.with(binding.getRoot())
					.load(item.getBestPoster())
					.transition(withCrossFade())
					.into(binding.poster);

			/*
			 Because of some strange bug we have to load banner by our hands
			 or else it'll in some moment will stretch
			*/

			Glide.with(binding.getRoot())
					.load(item.getBestBanner())
					.into(new CustomTarget<Drawable>() {
						@Override
						public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
							if(item != PagerViewHolder.this.item) return;
							binding.banner.setImageDrawable(resource);
						}

						@Override
						public void onLoadCleared(@Nullable Drawable placeholder) {}
					});

			this.item = item;
		}
	}
}