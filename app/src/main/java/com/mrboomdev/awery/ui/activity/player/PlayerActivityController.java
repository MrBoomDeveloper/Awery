package com.mrboomdev.awery.ui.activity.player;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.mrboomdev.awery.util.StringUtil;
import com.mrboomdev.awery.util.ui.adapter.SimpleAdapter;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.mrboomdev.awery.util.ui.dialog.DialogUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.mrboomdev.awery.databinding.PopupQualityHeaderBinding;
import com.mrboomdev.awery.databinding.PopupQualityItemBinding;

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

	public void showVideoSelectionDialog(boolean isRequired) {
		var videos = activity.episode.getVideos();
		if(videos == null) return;

		final var dialog = new AtomicReference<Dialog>();
		final var didSelectedVideo = new AtomicBoolean();

		var recycler = new RecyclerView(activity);
		recycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));

		var sheet = new BottomSheetDialog(activity);
		dialog.set(sheet);

		var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
			var inflater = activity.getLayoutInflater();
			return PopupQualityHeaderBinding.inflate(inflater, parent, false);
		});

		var itemsAdapter = new SimpleAdapter<>(parent -> {
			var binding = PopupQualityItemBinding.inflate(
					activity.getLayoutInflater(), parent, false);

			return new VideoHolder(binding, video -> {
				activity.playVideo(video);
				didSelectedVideo.set(true);
				dialog.get().dismiss();
			});
		}, (item, holder) -> holder.bind(item), videos, true);

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

	private static class VideoHolder extends RecyclerView.ViewHolder {
		private final PopupQualityItemBinding binding;
		private CatalogVideo video;

		public VideoHolder(@NonNull PopupQualityItemBinding binding, VideoSelectListener selectCallback) {
			super(binding.getRoot());
			this.binding = binding;

			binding.text.setOnClickListener(v ->
					selectCallback.onVideoSelected(video));
		}

		public void bind(@NonNull CatalogVideo video) {
			this.video = video;
			binding.text.setText(video.getTitle());
		}
	}

	private interface VideoSelectListener {
		void onVideoSelected(@NonNull CatalogVideo video);
	}
}