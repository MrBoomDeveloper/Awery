package com.mrboomdev.awery.ui.adapter;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import com.mrboomdev.awery.databinding.ItemListEpisodeBinding;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.ui.activity.MediaActivity;
import com.mrboomdev.awery.ui.fragments.MediaPlayFragment;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.WeakHashMap;

public class MediaPlayEpisodesAdapter extends RecyclerView.Adapter<MediaPlayEpisodesAdapter.ViewHolder> {
	private final WeakHashMap<CatalogEpisode, Long> progresses = new WeakHashMap<>();
	private final WeakHashMap<CatalogEpisode, Long> ids = new WeakHashMap<>();
	private final UniqueIdGenerator idGenerator = new UniqueIdGenerator();
	private OnEpisodeSelectedListener onEpisodeSelectedListener;
	private ArrayList<CatalogEpisode> items = new ArrayList<>();
	private CatalogMedia media;

	public MediaPlayEpisodesAdapter() {
		setHasStableIds(true);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(CatalogMedia media, Collection<? extends CatalogEpisode> items) {
		this.media = media;
		idGenerator.clear();

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

		new Thread(() -> {
			var progressDao = getDatabase().getMediaProgressDao();
			var progress = progressDao.get(media.globalId);

			if(progress != null) {
				for(var entry : progress.progresses.entrySet()) {
					stream(items)
							.filter(e -> e.getNumber() == entry.getKey())
							.findFirst().ifPresent(episode ->
									progresses.put(episode, entry.getValue()));
				}
			}

			runOnUiThread(this::notifyDataSetChanged);
		}).start();
	}

	@Override
	public int getItemViewType(int position) {
		return MediaPlayFragment.VIEW_TYPE_EPISODE;
	}

	public void setOnEpisodeSelectedListener(OnEpisodeSelectedListener listener) {
		this.onEpisodeSelectedListener = listener;
	}

	public interface OnEpisodeSelectedListener {
		void onEpisodeSelected(@NonNull CatalogEpisode episode, ArrayList<CatalogEpisode> episodes);
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
			CatalogEpisode episode,
			long episodeProgress,
			@NonNull ViewHolder holder,
			Runnable callback
	) {
		progresses.put(episode, episodeProgress);
		holder.updateProgress();

		new Thread(() -> {
			var progressDao = getDatabase().getMediaProgressDao();

			var progress = progressDao.get(media.globalId);
			if(progress == null) progress = new CatalogMediaProgress(media.globalId);

			progress.progresses.put(episode.getNumber(), episodeProgress);
			progressDao.insert(progress);

			if(callback != null) {
				callback.run();
			}
		}).start();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		var inflater = LayoutInflater.from(parent.getContext());
		var binding = ItemListEpisodeBinding.inflate(inflater, parent, false);
		setTopMargin(binding.getRoot(), dpPx(12));
		var holder = new ViewHolder(binding);

		binding.options.setOnClickListener(v -> {
			long progress = Objects.requireNonNullElse(
					progresses.get(holder.getItem()), 0L);

			var menu = new PopupMenu(MaterialThemeOverlay.wrap(v.getContext(), null,
					0, com.google.android.material.R.style.Widget_Material3_PopupMenu), v);

			menu.getMenu().add(0, 0, 0, progress != 0
					? "Remove from watched" : "Mark as watched");

			menu.getMenu().add(0, 1, 0, "See comments");
//			menu.getMenu().add(0, 2, 0, "Download");
//			menu.getMenu().add(0, 3, 0, "Share");
//			menu.getMenu().add(0, 4, 0, "Hide");

			menu.setOnMenuItemClickListener(item -> switch(item.getItemId()) {
				case 0 -> {
					changeWatchedState(holder.getItem(), progress != 0 ? 0 : -1L, holder, null);
					yield true;
				}

				case 1 -> {
					var activity = (MediaActivity) getActivity(getContext(parent));
					var episode = holder.getItem();

					if(activity == null) {
						throw new NullPointerException("No activity was found with type of MediaActivity");
					}

					activity.launchAction(MediaUtils.ACTION_COMMENTS, episode);
					yield true;
				}

				case 2 -> throw new UnimplementedException("Download not implemented");
				case 3 -> throw new UnimplementedException("Share not implemented");
				case 4 -> throw new UnimplementedException("Hide not implemented");
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
		private CatalogEpisode item;

		public ViewHolder(@NonNull ItemListEpisodeBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public CatalogEpisode getItem() {
			return item;
		}

		public void updateProgress() {
			long progress = Objects.requireNonNullElse(progresses.get(item), 0L);
			binding.container.setAlpha((progress != 0) ? .6f : 1);
			binding.bannerWrapper.setAlpha((progress != 0) ? .4f : 1);
		}

		@SuppressLint("SetTextI18n")
		public void bind(@NonNull CatalogEpisode item) {
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