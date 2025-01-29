package com.mrboomdev.awery.ui.mobile.screens.media;

import static com.mrboomdev.awery.app.App.openUrl;
import static com.mrboomdev.awery.app.App.share;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.platform.PlatformResourcesKt.i18n;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;
import static java.util.Objects.requireNonNullElse;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.databinding.ItemListEpisodeBinding;
import com.mrboomdev.awery.ext.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.generated.Res;
import com.mrboomdev.awery.generated.String0_commonMainKt;
import com.mrboomdev.awery.ui.utils.UniqueIdGenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import kotlin.NotImplementedError;

public class MediaPlayEpisodesAdapter extends RecyclerView.Adapter<MediaPlayEpisodesAdapter.ViewHolder> {
	private final WeakHashMap<CatalogVideo, Long> progresses = new WeakHashMap<>();
	private final WeakHashMap<CatalogVideo, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private OnEpisodeSelectedListener onEpisodeSelectedListener;
	private ArrayList<CatalogVideo> items = new ArrayList<>();
	private CatalogMedia media;

	public MediaPlayEpisodesAdapter() {
		setHasStableIds(true);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(CatalogMedia media, Collection<? extends CatalogVideo> items) {
		this.media = media;
		idGenerator.reset();

		if(media == null || items == null) {
			this.items = null;
			progresses.clear();
			ids.clear();
			notifyDataSetChanged();
			return;
		}

		for(var item : items) {
			ids.put(item, idGenerator.getLong());
		}

		this.items = new ArrayList<>(items);
		Collections.sort(this.items);

		thread(() -> {
			var progressDao = App.Companion.getDatabase().getMediaProgressDao();
			var progress = progressDao.get(media.getGlobalId());

			if(progress != null) {
				for(var entry : progress.progresses.entrySet()) {
					stream(items)
							.filter(e -> e.getNumber() == entry.getKey())
							.findFirst().ifPresent(episode ->
									progresses.put(episode, entry.getValue()));
				}
			}

			runOnUiThread(this::notifyDataSetChanged);
		});
	}

	public CatalogMedia getMedia() {
		return media;
	}

	@Override
	public int getItemViewType(int position) {
		return MediaPlayFragment.VIEW_TYPE_EPISODE;
	}

	public void setOnEpisodeSelectedListener(OnEpisodeSelectedListener listener) {
		this.onEpisodeSelectedListener = listener;
	}

	public interface OnEpisodeSelectedListener {
		void onEpisodeSelected(@NonNull CatalogVideo episode, List<CatalogVideo> episodes);
	}

	@Override
	public long getItemId(int position) {
		var id = ids.get(items.get(position));

		if(id == null) {
			throw new IllegalStateException("Id for item not found: " + position);
		}

		return id;
	}

	private void changeWatchedState(
			CatalogVideo episode,
			long episodeProgress,
			@NonNull ViewHolder holder,
			Runnable callback
	) {
		progresses.put(episode, episodeProgress);
		holder.updateProgress();

		thread(() -> {
			var progressDao = App.Companion.getDatabase().getMediaProgressDao();

			var progress = progressDao.get(media.getGlobalId());
			if(progress == null) progress = new CatalogMediaProgress(media.getGlobalId());
			
			progress.progresses.put(episode.getNumber(), episodeProgress);
			progressDao.insert(progress);

			if(callback != null) {
				callback.run();
			}
		});
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = ItemListEpisodeBinding.inflate(inflater, parent, false);
		setTopMargin(binding.getRoot(), dpPx(binding, 12));
		var holder = new ViewHolder(binding);

		binding.options.setOnClickListener(v -> {
			long progress = requireNonNullElse(progresses.get(holder.getItem()), 0L);

			var menu = new PopupMenu(MaterialThemeOverlay.wrap(v.getContext(), null,
					0, com.google.android.material.R.style.Widget_Material3_PopupMenu), v);

			menu.getMenu().add(0, 0, 0, progress != 0
					? i18n(String0_commonMainKt.getMark_not_watched(Res.string.INSTANCE)) : i18n(String0_commonMainKt.getMark_watched(Res.string.INSTANCE)));

			menu.getMenu().add(0, 1, 0, i18n(String0_commonMainKt.getComments(Res.string.INSTANCE)));
//			menu.getMenu().add(0, 2, 0, "Download");
			menu.getMenu().add(0, 3, 0, i18n(String0_commonMainKt.getShare(Res.string.INSTANCE)));
			menu.getMenu().add(0, 4, 0, i18n(String0_commonMainKt.getOpen_link_externally(Res.string.INSTANCE)));
//			menu.getMenu().add(0, 5, 0, "Hide");

			menu.setOnMenuItemClickListener(item -> switch(item.getItemId()) {
				case 0 -> {
					changeWatchedState(holder.getItem(), progress != 0 ? 0 : -1L, holder, null);
					yield true;
				}

				case 1 -> {
					var activity = (MediaActivity) getActivity(parent.getContext());
					var episode = holder.getItem();

					if(activity == null) {
						throw new NullPointerException("No activity was found with type of MediaActivity");
					}

					activity.launchAction(MediaActivity.Action.COMMENTS, episode);
					yield true;
				}

				case 2 -> throw new NotImplementedError("Download not implemented");

				case 3 -> {
					share(holder.getItem().getUrl());
					yield true;
				}

				case 4 -> {
					openUrl(parent.getContext(), holder.getItem().getUrl(), true);
					yield true;
				}

				case 5 -> throw new NotImplementedError("Hide not implemented");
				default -> throw new IllegalStateException("Unexpected value: " + item.getItemId());
			});

			menu.show();
		});

		binding.container.setOnClickListener(v -> {
			var item = holder.getItem();
			if(onEpisodeSelectedListener == null) return;

			changeWatchedState(holder.getItem(), -1, holder, () ->
					runOnUiThread(() -> onEpisodeSelectedListener.onEpisodeSelected(item, items)));
		});

		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(items.get(position));
	}

	@Override
	public int getItemCount() {
		if(items == null) return 0;
		return items.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemListEpisodeBinding binding;
		private CatalogVideo item;

		public ViewHolder(@NonNull ItemListEpisodeBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogVideo getItem() {
			return item;
		}

		public void updateProgress() {
			long progress = requireNonNullElse(progresses.get(item), 0L);
			binding.container.setAlpha((progress != 0) ? .6f : 1);
			binding.bannerWrapper.setAlpha((progress != 0) ? .4f : 1);
		}

		@SuppressLint("SetTextI18n")
		public void bind(@NonNull CatalogVideo item) {
			this.item = item;

			binding.title.setText(item.getTitle());
			updateProgress();

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
				binding.bannerWrapper.setVisibility(View.VISIBLE);
				binding.banner.setImageDrawable(null);

				Glide.with(binding.banner)
						.load(item.getBanner())
						.into(binding.banner);
			} else {
				binding.bannerWrapper.setVisibility(View.GONE);
				Glide.with(binding.banner).clear(binding.banner);
			}
		}
	}
}