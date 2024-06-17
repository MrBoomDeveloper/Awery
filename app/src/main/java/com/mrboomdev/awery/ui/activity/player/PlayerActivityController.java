package com.mrboomdev.awery.ui.activity.player;

import static com.mrboomdev.awery.app.AweryLifecycle.cancelDelayed;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.app.AweryLifecycle.startActivityForResult;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.StringRes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryLifecycle;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.databinding.PopupSimpleHeaderBinding;
import com.mrboomdev.awery.databinding.PopupSimpleItemBinding;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.MimeTypes;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.util.ui.adapter.SimpleAdapter;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.dialog.DialogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerActivityController {
	private static final int REQUEST_CODE_PICK_SUBTITLES = AweryLifecycle.getActivityResultCode();
	private static final int SHOW_UI_FOR_MILLIS = 3_000;
	private final Set<String> lockedUiReasons = new HashSet<>();
	private final PlayerActivity activity;
	private final Runnable hideUiRunnable;
	private boolean isUiFadeLocked, isUiVisible;
	private final boolean dim = AwerySettings.PLAYER_DIM_SCREEN.getValue();

	public PlayerActivityController(PlayerActivity activity) {
		this.activity = activity;

		this.hideUiRunnable = () -> {
			if(!isUiVisible) return;

			activity.setButtonsClickability(false);

			ObjectAnimator.ofFloat(
					activity.binding.uiOverlay,
					"alpha",
					1, 0).start();

			ObjectAnimator.ofFloat(
					activity.binding.darkOverlay,
					"alpha",
					dim ? .6f : 0, 0).start();

			isUiVisible = false;
		};
	}

	@SuppressLint("SetTextI18n")
	@OptIn(markerClass = UnstableApi.class)
	public void updateTimers() {
		var position = activity.player.getCurrentPosition();
		var duration = activity.player.getDuration();

		activity.binding.slider.setPosition(position);

		activity.binding.slider.setBufferedPosition(
				activity.player.getBufferedPosition());

		activity.binding.timer.setText(
				StringUtils.formatClock(position) + "/" +
				StringUtils.formatClock(duration));
	}

	public void addLockedUiReason(String reason) {
		if(lockedUiReasons.isEmpty()) {
			setIsUiFadeLocked(true);
		}

		lockedUiReasons.add(reason);
	}

	public void removeLockedUiReason(String reason) {
		lockedUiReasons.remove(reason);

		if(lockedUiReasons.isEmpty()) {
			setIsUiFadeLocked(false);
		}
	}

	private void setIsUiFadeLocked(boolean isLocked) {
		if(isLocked == this.isUiFadeLocked) return;
		this.isUiFadeLocked = isLocked;

		if(!isLocked) showUiTemporarily();
		else cancelDelayed(hideUiRunnable);
	}

	private void checkIfReasonsAreValid() {
		if(lockedUiReasons.contains("pause") && activity.player.isPlaying()) {
			removeLockedUiReason("pause");
		}
	}

	public void toggleUiVisibility() {
		checkIfReasonsAreValid();

		if(!isUiVisible) {
			if(isUiFadeLocked) {
				showUi();
			} else {
				showUiTemporarily();
			}
		} else {
			hideUi();
		}
	}

	public void showUi() {
		this.isUiVisible = true;

		activity.setButtonsClickability(true);

		ObjectAnimator.ofFloat(
				activity.binding.uiOverlay,
				"alpha",
				0, 1).setDuration(222).start();

		if(dim) {
			ObjectAnimator.ofFloat(
					activity.binding.darkOverlay,
					"alpha", 0, .6f).setDuration(222).start();
		}
	}

	public void hideUi() {
		hideUiRunnable.run();
	}

	public void showUiTemporarily() {
		if(isUiFadeLocked) return;

		if(!isUiVisible) {
			showUi();
		}

		cancelDelayed(hideUiRunnable);
		runDelayed(hideUiRunnable, SHOW_UI_FOR_MILLIS);
	}

	@OptIn(markerClass = UnstableApi.class)
	public void setAspectRatio(@NonNull AwerySettings.VideoAspectRatio_Values aspectRatio) {
		activity.binding.aspectRatioFrame.setResizeMode(switch(aspectRatio) {
			case FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT;
			case FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL;
			case ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
		});
	}

	@OptIn(markerClass = UnstableApi.class)
	public void openScaleSettingsDialog() {
		final var dialog = new AtomicReference<Dialog>();

		var recycler = new RecyclerView(activity);
		recycler.setVerticalScrollBarEnabled(false);
		recycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

		var sheet = new BottomSheetDialog(activity);
		dialog.set(sheet);

		var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = activity.getLayoutInflater();
			var binding = PopupSimpleHeaderBinding.inflate(inflater, parent, false);
			binding.text.setText(R.string.aspect_ratio);
			return binding;
		});

		var items = new LinkedHashMap<PopupItem, AwerySettings.VideoAspectRatio_Values>() {{
			put(new PopupItem(R.string.fit), AwerySettings.VideoAspectRatio_Values.FIT);
			put(new PopupItem(R.string.fill), AwerySettings.VideoAspectRatio_Values.FILL);
			put(new PopupItem(R.string.zoom), AwerySettings.VideoAspectRatio_Values.ZOOM);
		}};

		var itemsAdapter = new SimpleAdapter<>(parent -> {
			var binding = PopupSimpleItemBinding.inflate(
					activity.getLayoutInflater(), parent, false);

			return new PopupItemHolder(binding, item -> {
				var ratio = items.get(item);

				if(ratio == null) {
					throw new NullPointerException("Ratio was null");
				}

				setAspectRatio(ratio);
				getPrefs().setValue(AwerySettings.VIDEO_ASPECT_RATIO, ratio).saveAsync();
			});
		}, (item, holder) -> holder.bind(item), new ArrayList<>(items.keySet()), true);

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), headerAdapter, itemsAdapter);

		recycler.setAdapter(concatAdapter);
		sheet.setContentView(recycler);
		sheet.getBehavior().setPeekHeight(9999);
		sheet.show();

		DialogUtils.fixDialog(dialog.get());
	}

	public void openSettingsDialog() {
		final var dialog = new AtomicReference<Dialog>();

		var recycler = new RecyclerView(activity);
		recycler.setVerticalScrollBarEnabled(false);
		recycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

		var sheet = new BottomSheetDialog(activity);
		dialog.set(sheet);

		var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = activity.getLayoutInflater();
			var binding = PopupSimpleHeaderBinding.inflate(inflater, parent, false);
			binding.text.setVisibility(View.GONE);
			binding.divider.setVisibility(View.INVISIBLE);
			return binding;
		});

		enum Action { QUALITY, ASPECT }

		var items = new LinkedHashMap<PopupItem, Action>() {{
			put(new PopupItem(R.string.video_quality, R.drawable.ic_round_high_quality_24), Action.QUALITY);
			put(new PopupItem(R.string.aspect_ratio, R.drawable.ic_fullscreen), Action.ASPECT);
		}};

		var itemsAdapter = new SimpleAdapter<>(parent -> {
			var binding = PopupSimpleItemBinding.inflate(
					activity.getLayoutInflater(), parent, false);

			return new PopupItemHolder(binding, item -> {
				var action = items.get(item);

				if(action == null) {
					throw new NullPointerException("Action was null");
				}

				switch(action) {
					case QUALITY -> openQualityDialog(false);
					case ASPECT -> openScaleSettingsDialog();
				}

				dialog.get().dismiss();
			});
		}, (item, holder) -> holder.bind(item), new ArrayList<>(items.keySet()), true);

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), headerAdapter, itemsAdapter);

		recycler.setAdapter(concatAdapter);
		sheet.setContentView(recycler);
		sheet.getBehavior().setPeekHeight(9999);
		sheet.show();

		DialogUtils.fixDialog(dialog.get());
	}

	public void openSubtitlesDialog() {
		if(activity.video == null) return;

		var subtitles = activity.video.getSubtitles();
		if(subtitles == null) return;

		var picker = new PopupItem(R.string.pick_from_storage);

		var items = new LinkedHashMap<PopupItem, CatalogSubtitle>();
		items.put(new PopupItem(R.string.none), null);
		items.put(picker, null);

		for(var subtitle : subtitles) {
			items.put(new PopupItem().setTitle(subtitle.getTitle()), subtitle);
		}

		final var dialog = new AtomicReference<Dialog>();

		var recycler = new RecyclerView(activity);
		recycler.setVerticalScrollBarEnabled(false);
		recycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

		var sheet = new BottomSheetDialog(activity);
		dialog.set(sheet);

		var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = activity.getLayoutInflater();
			var binding = PopupSimpleHeaderBinding.inflate(inflater, parent, false);
			binding.text.setText(R.string.subtitles);
			return binding;
		});

		var itemsAdapter = new PopupAdapter(new ArrayList<>(items.keySet()), item -> {
			if(item == picker) {
				var intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(MimeTypes.ANY.toString());
				var chooser = Intent.createChooser(intent, "Choose a subtitles file");

				startActivityForResult(activity, chooser, REQUEST_CODE_PICK_SUBTITLES, (resultCode, result) -> {
					if(resultCode != PlayerActivity.RESULT_OK) return;

					var uri = Objects.requireNonNull(result.getData());
					var file = new File(uri.toString());

					activity.setSubtitles(new CatalogSubtitle(file.getName(), uri.toString()) {
						@Override
						public Uri getUri() {
							return uri;
						}
					});
				});

				dialog.get().dismiss();
				return;
			}

			var subtitle = items.get(item);
			activity.setSubtitles(subtitle);
			dialog.get().dismiss();
		});

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), headerAdapter, itemsAdapter);

		recycler.setAdapter(concatAdapter);
		sheet.setContentView(recycler);
		sheet.getBehavior().setPeekHeight(9999);
		sheet.show();

		DialogUtils.fixDialog(dialog.get());
	}

	public void openQualityDialog(boolean isRequired) {
		if(activity.episode == null) return;

		var videos = activity.episode.getVideos();
		if(videos == null) return;

		var items = new LinkedHashMap<PopupItem, CatalogVideo>();

		for(var video : videos) {
			items.put(new PopupItem().setTitle(video.getTitle()), video);
		}

		final var dialog = new AtomicReference<Dialog>();
		final var didSelectedVideo = new AtomicBoolean();

		var recycler = new RecyclerView(activity);
		recycler.setVerticalScrollBarEnabled(false);
		recycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

		var sheet = new BottomSheetDialog(activity);
		dialog.set(sheet);

		var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = activity.getLayoutInflater();
			return PopupSimpleHeaderBinding.inflate(inflater, parent, false);
		});

		var itemsAdapter = new PopupAdapter(new ArrayList<>(items.keySet()), item -> {
			var video = Objects.requireNonNull(items.get(item));
			activity.setVideo(video);
			didSelectedVideo.set(true);
			dialog.get().dismiss();
		});

		if(isRequired) {
			sheet.setOnDismissListener(_dialog -> {
				if(didSelectedVideo.get()) {
					return;
				}

				activity.finish();
			});
		}

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), headerAdapter, itemsAdapter);

		recycler.setAdapter(concatAdapter);
		sheet.setContentView(recycler);
		sheet.getBehavior().setPeekHeight(9999);
		sheet.show();

		DialogUtils.fixDialog(dialog.get());
	}

	private class PopupAdapter extends RecyclerView.Adapter<PopupItemHolder> {
		private final ItemSelectListener itemSelectListener;
		private final List<PopupItem> items;

		public PopupAdapter(List<PopupItem> items, ItemSelectListener itemSelectListener) {
			this.itemSelectListener = itemSelectListener;
			this.items = items;
			setHasStableIds(true);
		}

		@NonNull
		@Override
		public PopupItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			var inflater = LayoutInflater.from(parent.getContext());
			var binding = PopupSimpleItemBinding.inflate(inflater, parent, false);
			return new PopupItemHolder(binding, itemSelectListener);
		}

		@Override
		public void onBindViewHolder(@NonNull PopupItemHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	public class PopupItem {
		private String title;
		private int id, icon;

		public PopupItem(@StringRes int title, @DrawableRes int icon) {
			this.title = activity.getString(title);
			this.icon = icon;
		}

		public PopupItem(@StringRes int title) {
			this.title = activity.getString(title);
		}

		public PopupItem() {}

		public PopupItem setTitle(String title) {
			this.title = title;
			return this;
		}

		public String getTitle() {
			return title;
		}

		public PopupItem setId(int id) {
			this.id = id;
			return this;
		}

		public int getId() {
			return id;
		}

		public PopupItem setIcon(@DrawableRes int icon) {
			this.icon = icon;
			return this;
		}

		@DrawableRes
		public int getIcon() {
			return icon;
		}
	}

	private static class PopupItemHolder extends RecyclerView.ViewHolder {
		private final PopupSimpleItemBinding binding;
		private PopupItem item;

		public PopupItemHolder(@NonNull PopupSimpleItemBinding binding, ItemSelectListener selectCallback) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot().setOnClickListener(v ->
					selectCallback.onItemSelected(item));
		}

		public void bind(@NonNull PopupItem item) {
			if(item.icon == 0) {
				binding.icon.setVisibility(View.GONE);
			} else {
				binding.icon.setVisibility(View.VISIBLE);
				binding.icon.setImageResource(item.icon);
			}

			this.item = item;
			binding.text.setText(item.title);
		}
	}

	private interface ItemSelectListener {
		void onItemSelected(@NonNull PopupItem item);
	}
}