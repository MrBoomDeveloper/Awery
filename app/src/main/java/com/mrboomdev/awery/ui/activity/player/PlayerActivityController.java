package com.mrboomdev.awery.ui.activity.player;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.databinding.PopupSimpleHeaderBinding;
import com.mrboomdev.awery.databinding.PopupSimpleItemBinding;
import com.mrboomdev.awery.util.StringUtil;
import com.mrboomdev.awery.util.ui.adapter.SimpleAdapter;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.dialog.DialogUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerActivityController {
	private static final int SHOW_UI_FOR_MILLIS = 3_000;
	private final Set<String> lockedUiReasons = new HashSet<>();
	private final PlayerActivity activity;
	private final Runnable hideUiRunnable;
	private boolean isUiFadeLocked, isUiVisible;

	public PlayerActivityController(PlayerActivity activity) {
		this.activity = activity;
		this.hideUiRunnable = () -> {
			if(!isUiVisible) return;

			activity.setButtonsClickability(false);
			ObjectAnimator.ofFloat(activity.binding.uiOverlay, "alpha", 1, 0).start();
			ObjectAnimator.ofFloat(activity.binding.darkOverlay, "alpha", .6f, 0).start();
			isUiVisible = false;
		};
	}

	@SuppressLint("SetTextI18n")
	@OptIn(markerClass = UnstableApi.class)
	public void updateTimers() {
		var position = activity.player.getCurrentPosition();
		var duration = activity.player.getDuration();

		activity.binding.slider.setPosition(position);

		activity.binding.timer.setText(
				StringUtil.formatTimestamp(position) + "/" +
				StringUtil.formatTimestamp(duration));
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
		else AweryApp.cancelDelayed(hideUiRunnable);
	}

	public void toggleUiVisibility() {
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
		ObjectAnimator.ofFloat(activity.binding.uiOverlay, "alpha", 0, 1).start();
		ObjectAnimator.ofFloat(activity.binding.darkOverlay, "alpha", 0, .6f).start();
	}

	public void hideUi() {
		hideUiRunnable.run();
	}

	public void showUiTemporarily() {
		if(isUiFadeLocked) return;

		if(!isUiVisible) {
			showUi();
		}

		AweryApp.cancelDelayed(hideUiRunnable);
		AweryApp.runDelayed(hideUiRunnable, SHOW_UI_FOR_MILLIS);
	}

	@OptIn(markerClass = UnstableApi.class)
	public void openScaleSettingsDialog() {
		final var dialog = new AtomicReference<Dialog>();

		var recycler = new RecyclerView(activity);
		recycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

		var sheet = new BottomSheetDialog(activity);
		dialog.set(sheet);

		var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = activity.getLayoutInflater();
			var binding = PopupSimpleHeaderBinding.inflate(inflater, parent, false);
			binding.text.setText("Select video aspect ratio");
			return binding;
		});

		enum Mode { FIT, FILL, ZOOM }

		var items = new LinkedHashMap<PopupItem, Mode>() {{
			put(new PopupItem().setTitle("Fit"), Mode.FIT);
			put(new PopupItem().setTitle("Fill"), Mode.FILL);
			put(new PopupItem().setTitle("Zoom"), Mode.ZOOM);
		}};

		var itemsAdapter = new SimpleAdapter<>(parent -> {
			var binding = PopupSimpleItemBinding.inflate(
					activity.getLayoutInflater(), parent, false);

			return new PopupItemHolder(binding, item -> {
				var action = items.get(item);

				if(action == null) {
					throw new NullPointerException("Action was null");
				}

				activity.binding.aspectRatioFrame.setResizeMode(switch(action) {
					case FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT;
					case FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL;
					case ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
				});
			});
		}, (item, holder) -> holder.bind(item), new ArrayList<>(items.keySet()), true);

		var concatAdapter = new ConcatAdapter(new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build(), headerAdapter, itemsAdapter);

		recycler.setAdapter(concatAdapter);
		sheet.setContentView(recycler);
		sheet.getBehavior().setPeekHeight(9999);
		sheet.show();

		DialogUtil.limitDialogSize(dialog.get());
	}

	public void openSettingsDialog() {
		final var dialog = new AtomicReference<Dialog>();

		var recycler = new RecyclerView(activity);
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
			put(new PopupItem().setTitle("Video quality").setIcon(R.drawable.ic_round_high_quality_24), Action.QUALITY);
			put(new PopupItem().setTitle("Aspect ratio").setIcon(R.drawable.ic_round_fullscreen_24), Action.ASPECT);
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

		DialogUtil.limitDialogSize(dialog.get());
	}

	public void openQualityDialog(boolean isRequired) {
		var videos = activity.episode.getVideos();
		if(videos == null) return;

		var items = new LinkedHashMap<PopupItem, CatalogVideo>();

		for(var video : videos) {
			items.put(new PopupItem().setTitle(video.getTitle()), video);
		}

		final var dialog = new AtomicReference<Dialog>();
		final var didSelectedVideo = new AtomicBoolean();

		var recycler = new RecyclerView(activity);
		recycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

		var sheet = new BottomSheetDialog(activity);
		dialog.set(sheet);

		var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = activity.getLayoutInflater();
			return PopupSimpleHeaderBinding.inflate(inflater, parent, false);
		});

		var itemsAdapter = new SimpleAdapter<>(parent -> {
			var binding = PopupSimpleItemBinding.inflate(
					activity.getLayoutInflater(), parent, false);

			return new PopupItemHolder(binding, item -> {
				var video = Objects.requireNonNull(items.get(item));
				activity.playVideo(video);
				didSelectedVideo.set(true);
				dialog.get().dismiss();
			});
		}, (item, holder) -> holder.bind(item), new ArrayList<>(items.keySet()), true);

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

		DialogUtil.limitDialogSize(dialog.get());
	}

	public static class PopupItem {
		private String title;
		private int id, icon;

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