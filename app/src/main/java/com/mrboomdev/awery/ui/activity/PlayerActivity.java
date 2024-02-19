package com.mrboomdev.awery.ui.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.slider.Slider;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.ui.ThemeManager;

import java.util.Locale;

import ani.awery.R;
import ani.awery.databinding.LayoutActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity implements Player.Listener {
	private LayoutActivityPlayerBinding binding;
	private Runnable hideUiRunnable;
	private boolean isVideoPaused;
	private ExoPlayer player;

	@Override
	@OptIn(markerClass = UnstableApi.class)
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		super.onCreate(savedInstanceState);

		binding = LayoutActivityPlayerBinding.inflate(getLayoutInflater());
		toggleUiElementsClickability(false);
		setContentView(binding.getRoot());

		var controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
		controller.hide(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
		controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

		binding.aspectRatioFrame.setAspectRatio(16f / 9f);
		binding.aspectRatioFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

		var url = getIntent().getStringExtra("url");
		var item = MediaItem.fromUri(url);

		player = new ExoPlayer.Builder(this).build();
		player.setVideoTextureView(binding.textureView);
		player.addListener(this);
		player.setMediaItem(item);

		binding.pause.setOnClickListener(view -> {
			if(!isVideoPaused) {
				Glide.with(this).load(R.drawable.anim_pause_to_play).into(binding.pause);
				player.pause();

				if(hideUiRunnable != null) {
					AweryApp.cancelDelayed(hideUiRunnable);
				}
			} else {
				Glide.with(this).load(R.drawable.anim_play_to_pause).into(binding.pause);
				player.play();

				if(hideUiRunnable != null) {
					AweryApp.runDelayed(hideUiRunnable, 3_000);
				}
			}

			isVideoPaused = !isVideoPaused;
		});

		binding.slider.addOnChangeListener((slider, value, fromUser) -> {
			if(!fromUser) return;
			player.seekTo((long) value * 1000);
		});

		binding.slider.setLabelFormatter(value -> {
			var hours = (int) value / 3600;

			if(hours >= 1) {
				return String.format(Locale.ENGLISH, "%02d:%02d:%02d",
						hours, (int) value / 60, (int) value % 60);
			}

			return String.format(Locale.ENGLISH, "%02d:%02d",
					(int) value / 60, (int) value % 60);
		});

		binding.slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
			@Override
			public void onStartTrackingTouch(@NonNull Slider slider) {
				player.pause();
			}

			@Override
			public void onStopTrackingTouch(@NonNull Slider slider) {
				if(!isVideoPaused) {
					player.play();
				}
			}
		});

		binding.uiOverlay.setOnClickListener(view -> {
			if(hideUiRunnable != null) {
				AweryApp.cancelDelayed(hideUiRunnable);
				hideUiRunnable.run();
				hideUiRunnable = null;
				return;
			}

			toggleUiElementsClickability(true);
			ObjectAnimator.ofFloat(binding.uiOverlay, "alpha", 0, 1).start();

			hideUiRunnable = () -> {
				toggleUiElementsClickability(false);
				ObjectAnimator.ofFloat(binding.uiOverlay, "alpha", 1, 0).start();
				hideUiRunnable = null;
			};

			AweryApp.runDelayed(hideUiRunnable, 3_000);
		});

		Runnable updateProgress = new Runnable() {
			@Override
			public void run() {
				if(isDestroyed()) return;

				binding.slider.setValue(player.getCurrentPosition() / 1000f);
				AweryApp.runDelayed(this, 1_000);
			}
		};

		AweryApp.runDelayed(updateProgress, 1_000);
	}

	private void toggleUiElementsClickability(boolean isClickable) {
		binding.pause.setClickable(isClickable);
		binding.slider.setEnabled(isClickable);
	}

	@Override
	public void onPlaybackStateChanged(int playbackState) {
		switch(playbackState) {
			case Player.STATE_READY -> {
				var seconds = player.getDuration() / 1000L;
				binding.slider.setValueTo(seconds);

				binding.loadingCircle.setVisibility(View.GONE);
				binding.pause.setVisibility(View.VISIBLE);
			}

			case Player.STATE_BUFFERING -> {
				binding.loadingCircle.setVisibility(View.VISIBLE);
				binding.pause.setVisibility(View.GONE);
			}

			case Player.STATE_ENDED -> finish();

			case Player.STATE_IDLE -> {}
		}
	}

	@Override
	public void onPlayerError(@NonNull PlaybackException e) {
		e.printStackTrace();

		AweryApp.toast(switch(e.errorCode) {
			case PlaybackException.ERROR_CODE_TIMEOUT -> "Connection timeout has occurred, please try again later";
			case PlaybackException.ERROR_CODE_DECODING_FAILED -> "Video decoding failed, please try again later";
			default -> "Unknown error has occurred, please try again later";
		});

		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		player.prepare();
		player.play();

		AweryApp.setOnBackPressedListener(this, this::finish);
	}

	@Override
	protected void onResume() {
		super.onResume();
		player.play();
	}

	@Override
	protected void onPause() {
		super.onPause();
		player.pause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		player.stop();
		player.release();
		player = null;
	}
}