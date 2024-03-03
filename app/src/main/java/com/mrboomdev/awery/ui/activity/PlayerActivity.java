package com.mrboomdev.awery.ui.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.MotionEvent;
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
import androidx.media3.ui.TimeBar;

import com.bumptech.glide.Glide;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.CallbackUtil;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ani.awery.R;
import ani.awery.databinding.ScreenPlayerBinding;

public class PlayerActivity extends AppCompatActivity implements Player.Listener {
	private static final String ACTION_PAUSE = "pause";
	private static final String ACTION_PREVIOUS = "previous";
	private static final String ACTION_NEXT = "next";
	private final int SHOW_UI_AFTER_MILLIS = 200;
	private final int UI_INSETS = WindowInsetsCompat.Type.displayCutout()
			| WindowInsetsCompat.Type.systemGestures()
			| WindowInsetsCompat.Type.statusBars()
			| WindowInsetsCompat.Type.navigationBars();
	private final List<View> buttons = new ArrayList<>();
	private ScreenPlayerBinding binding;
	private CallbackUtil.Callback1<Boolean> hideUiRunnable;
	private Runnable hideUiRunnableWrapper, showUiRunnableFromLeft, showUiRunnableFromRight;
	private boolean areButtonsClickable, isSliderDragging;
	private boolean isVideoPaused, isVideoBuffering = true;
	private int forwardFastClicks, backwardFastClicks;
	private ExoPlayer player;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	@OptIn(markerClass = UnstableApi.class)
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		super.onCreate(savedInstanceState);

		binding = ScreenPlayerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyFullscreen();

		binding.aspectRatioFrame.setAspectRatio(16f / 9f);
		binding.aspectRatioFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

		var url = getIntent().getStringExtra("url");
		var item = MediaItem.fromUri(url);

		player = new ExoPlayer.Builder(this).build();
		player.setVideoTextureView(binding.textureView);
		player.addListener(this);
		player.setMediaItem(item);

		binding.title.setText(getIntent().getStringExtra("title"));

		binding.doubleTapBackward.setOnClickListener(view -> {
			backwardFastClicks++;

			if(backwardFastClicks >= 2) {
				if(showUiRunnableFromLeft != null) {
					AweryApp.cancelDelayed(showUiRunnableFromLeft);
					showUiRunnableFromLeft = null;
				}

				binding.doubleTapBackward.setBackgroundResource(R.drawable.ripple_circle_white);
				player.seekTo(player.getCurrentPosition() - 10_000);
				updateTimers();
			} else {
				showUiRunnableFromLeft = this::toggleUiVisibility;
				AweryApp.runDelayed(showUiRunnableFromLeft, SHOW_UI_AFTER_MILLIS);
			}

			AweryApp.runDelayed(() -> {
				backwardFastClicks--;

				if(backwardFastClicks == 0) {
					binding.doubleTapBackward.setBackground(null);
				}
			}, 500);
		});

		binding.doubleTapForward.setOnClickListener(view -> {
			forwardFastClicks++;

			if(forwardFastClicks >= 2) {
				if(showUiRunnableFromRight != null) {
					AweryApp.cancelDelayed(showUiRunnableFromRight);
					showUiRunnableFromRight = null;
				}

				binding.doubleTapForward.setBackgroundResource(R.drawable.ripple_circle_white);
				player.seekTo(player.getCurrentPosition() + 10_000);
				updateTimers();
			} else {
				showUiRunnableFromRight = this::toggleUiVisibility;
				AweryApp.runDelayed(showUiRunnableFromRight, SHOW_UI_AFTER_MILLIS);
			}

			AweryApp.runDelayed(() -> {
				forwardFastClicks--;

				if(forwardFastClicks == 0) {
					binding.doubleTapForward.setBackground(null);
				}
			}, 500);
		});

		binding.slider.addListener(new TimeBar.OnScrubListener() {
			@Override
			public void onScrubStart(@NonNull TimeBar timeBar, long position) {
				isSliderDragging = true;

				player.seekTo(position);
				updateTimers();

				player.pause();
			}

			@Override
			public void onScrubMove(@NonNull TimeBar timeBar, long position) {
				player.seekTo(position);
				updateTimers();
			}

			@Override
			public void onScrubStop(@NonNull TimeBar timeBar, long position, boolean canceled) {
				isSliderDragging = false;

				player.seekTo(position);
				updateTimers();

				if(!isVideoPaused) {
					player.play();

					if(hideUiRunnable != null) {
						hideUiRunnable.run(false);
					}
				}
			}
		});

		binding.getRoot().setOnTouchListener((view, event) -> {
			if(event.getAction() == MotionEvent.ACTION_MOVE) return true;
			if(event.getAction() == MotionEvent.ACTION_DOWN) return true;

			toggleUiVisibility();
			return true;
		});

		Runnable updateProgress = new Runnable() {
			@Override
			public void run() {
				if(isDestroyed()) return;
				updateTimers();
				AweryApp.runDelayed(this, 1_000);
			}
		};

		AweryApp.runDelayed(updateProgress, 1_000);
		setButtonsClickability(false);
		binding.getRoot().performClick();

		setupButton(binding.exit, this::finish);

		setupButton(binding.quickSkip, () -> {
			player.seekTo(player.getCurrentPosition() + 60_000);
			updateTimers();
		});

		setupButton(binding.pause, () -> {
			if(isVideoBuffering) return;

			if(!isVideoPaused) {
				Glide.with(this).load(R.drawable.anim_pause_to_play).into(binding.pause);
				player.pause();

				if(hideUiRunnableWrapper != null) {
					AweryApp.cancelDelayed(hideUiRunnableWrapper);
				}
			} else {
				Glide.with(this).load(R.drawable.anim_play_to_pause).into(binding.pause);
				player.play();

				if(hideUiRunnableWrapper != null) {
					AweryApp.runDelayed(hideUiRunnableWrapper, 3_000);
				}
			}

			isVideoPaused = !isVideoPaused;
		});

		registerPip();
		toggleUiVisibility();
	}

	@OptIn(markerClass = UnstableApi.class)
	private void registerPip() {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			binding.pip.setVisibility(View.GONE);
			return;
		}

		var pipParams = new PictureInPictureParams.Builder();

		setupButton(binding.pip, () -> {
			var format = player.getVideoFormat();

			if(format != null) {
				var ratio = new Rational(format.width, format.height);
				pipParams.setAspectRatio(ratio);
			}

			enterPictureInPictureMode(pipParams.build());
		});
	}

	@Override
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode);

		var uiVisibility = isInPictureInPictureMode ? View.GONE : View.VISIBLE;
		binding.uiOverlay.setVisibility(uiVisibility);
		binding.darkOverlay.setVisibility(uiVisibility);

		player.play();
	}

	private void toggleUiVisibility() {
		if(hideUiRunnableWrapper != null) {
			AweryApp.cancelDelayed(hideUiRunnableWrapper);
			hideUiRunnable.run(true);
			setHideUiRunnable(null);
			return;
		}

		setButtonsClickability(true);
		ObjectAnimator.ofFloat(binding.uiOverlay, "alpha", 0, 1).start();
		ObjectAnimator.ofFloat(binding.darkOverlay, "alpha", 0, .6f).start();

		setHideUiRunnable(isForced -> {
			if(!isForced && (isVideoPaused || isSliderDragging)) return;

			setButtonsClickability(false);
			ObjectAnimator.ofFloat(binding.uiOverlay, "alpha", 1, 0).start();
			ObjectAnimator.ofFloat(binding.darkOverlay, "alpha", .6f, 0).start();
			setHideUiRunnable(null);
		});

		AweryApp.runDelayed(hideUiRunnableWrapper, 3_000);
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setupButton(@NonNull View view, Runnable clickListener) {
		buttons.add(view);

		view.setOnClickListener(v -> {
			if(!areButtonsClickable) {
				return;
			}

			clickListener.run();
		});
	}

	@OptIn(markerClass = UnstableApi.class)
	private void setButtonsClickability(boolean isClickable) {
		this.areButtonsClickable = isClickable;
		binding.slider.setEnabled(isClickable);

		for(var view : buttons) {
			view.setClickable(isClickable);
		}
	}

	@Override
	@OptIn(markerClass = UnstableApi.class)
	public void onPlaybackStateChanged(int playbackState) {
		switch(playbackState) {
			case Player.STATE_READY -> {
				isVideoBuffering = false;

				binding.slider.setDuration(player.getDuration());

				binding.loadingCircle.setVisibility(View.GONE);
				binding.pause.setVisibility(View.VISIBLE);
			}

			case Player.STATE_BUFFERING -> {
				isVideoBuffering = true;

				binding.loadingCircle.setVisibility(View.VISIBLE);
				binding.pause.setVisibility(View.INVISIBLE);
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
			case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Video not found, please try again later";

			case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
					PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
					"Connection error has occurred, please try again later";

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

		if(!isVideoPaused) {
			player.play();
		}
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

	@SuppressLint("SetTextI18n")
	@OptIn(markerClass = UnstableApi.class)
	private void updateTimers() {
		binding.slider.setPosition(player.getCurrentPosition());
		binding.timer.setText(formatTime(player.getCurrentPosition()) + "/" + formatTime(player.getDuration()));
	}

	@NonNull
	private String formatTime(long value) {
		value /= 1000;

		var hours = (int) value / 3600;

		if(hours >= 1) {
			return String.format(Locale.ENGLISH, "%02d:%02d:%02d",
					hours, (int) value / 60, (int) value % 60);
		}

		return String.format(Locale.ENGLISH, "%02d:%02d",
				(int) value / 60, (int) value % 60);
	}

	private void applyFullscreen() {
		var controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
		controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
		controller.hide(UI_INSETS);

		ViewUtil.setOnApplyInsetsListener(binding.uiOverlay, insets -> {
			var systemInsets = insets.getInsets(UI_INSETS);
			var margin = ViewUtil.dpPx(10);

			ViewUtil.setLeftMargin(binding.exit, margin + systemInsets.left);
			ViewUtil.setRightMargin(binding.settings, systemInsets.right + margin);

			ViewUtil.setBottomMargin(binding.slider, systemInsets.bottom + margin);
			ViewUtil.setHorizontalMargin(binding.slider, margin + systemInsets.left, margin + systemInsets.right);
		});
	}

	private void setHideUiRunnable(CallbackUtil.Callback1<Boolean> runnable) {
		hideUiRunnable = runnable;

		hideUiRunnableWrapper = (runnable != null)
				? (() -> runnable.run(false)) : null;
	}
}