package com.mrboomdev.awery.ui.activity.player;

import static androidx.media3.session.SessionResult.RESULT_ERROR_NOT_SUPPORTED;
import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.cancelDelayed;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.ForwardingPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerNotificationManager;
import androidx.media3.ui.TimeBar;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.app.AweryNotifications;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.databinding.ScreenPlayerBinding;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends AppCompatActivity implements Player.Listener {
	public static final String PLAYBACK_ACTION = "AWERY_PLAYBACK";
	public static final int PLAYBACK_ACTION_RESUME = 1;
	public static final int PLAYBACK_ACTION_PAUSE = 2;
	private static final String TAG = "PlayerActivity";
	protected static ExtensionProvider source;
	protected final int SHOW_UI_AFTER_MILLIS = 200;
	protected final int UI_INSETS = WindowInsetsCompat.Type.displayCutout()
			| WindowInsetsCompat.Type.systemGestures()
			| WindowInsetsCompat.Type.statusBars()
			| WindowInsetsCompat.Type.navigationBars();
	protected final Set<View> buttons = new HashSet<>();
	private final PlayerActivityController controller = new PlayerActivityController(this);
	private MediaSession session;
	private PlayerGestures gestures;
	protected ScreenPlayerBinding binding;
	protected Runnable showUiRunnableFromLeft, showUiRunnableFromRight;
	protected boolean areButtonsClickable;
	protected boolean isVideoPaused, isVideoBuffering = true, didSelectedVideo;
	protected int forwardFastClicks, backwardFastClicks;
	protected ArrayList<CatalogEpisode> episodes;
	protected CatalogEpisode episode;
	protected CatalogVideo video;
	protected ExoPlayer player;
	protected int doubleTapSeek, bigSeek;
	protected GesturesMode gesturesMode;
	private MediaItem videoItem;

	@SuppressLint({"ClickableViewAccessibility", "UnspecifiedRegisterReceiverFlag"})
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);

		super.onCreate(savedInstanceState);
		loadSettings();

		/*registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch(intent.getIntExtra(PLAYBACK_ACTION, -1)) {
					case PLAYBACK_ACTION_RESUME -> {
						player.play();
						isVideoPaused = false;
					}

					case PLAYBACK_ACTION_PAUSE -> {
						player.pause();
						isVideoPaused = true;
					}

					default -> throw new IllegalArgumentException("Unknown playback action has been permitted!");
				}

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					setPictureInPictureParams(getPipParams());
				}
			}}, new IntentFilter(PLAYBACK_ACTION));*/

		binding = ScreenPlayerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyFullscreen();

		gestures = new PlayerGestures(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		binding.aspectRatioFrame.setAspectRatio(16f / 9f);
		binding.aspectRatioFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

		binding.subtitleView.setUserDefaultStyle();
		binding.subtitleView.setUserDefaultTextSize();

		var audioAttributes = new AudioAttributes.Builder()
				.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
				.setUsage(C.USAGE_MEDIA)
				.build();

		player = new ExoPlayer.Builder(this)
				.setSeekBackIncrementMs(doubleTapSeek * 1000L + 1)
				.setSeekForwardIncrementMs(doubleTapSeek * 1000L + 1)
				.setAudioAttributes(audioAttributes, true)
				.build();

		player.setVideoSurfaceView(binding.surfaceView);
		player.setHandleAudioBecomingNoisy(true);
		player.addListener(this);

		session = new MediaSession.Builder(this, player)
				.setCallback(new MediaSession.Callback() {
					@NonNull
					@Override
					public MediaSession.ConnectionResult onConnect(
							@NonNull MediaSession session,
							@NonNull MediaSession.ControllerInfo controller
					) {
						return new MediaSession.ConnectionResult.AcceptedResultBuilder(session)
								.setAvailableSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS)
								.build();
					}
				})
				.build();

		binding.doubleTapBackward.setOnTouchListener((view, event) -> gestures.onTouchEventLeft(event));
		binding.doubleTapForward.setOnTouchListener((view, event) -> gestures.onTouchEventRight(event));

		binding.doubleTapBackward.setOnClickListener(view -> {
			if(doubleTapSeek == 0) {
				controller.toggleUiVisibility();
				return;
			}

			backwardFastClicks++;

			if(backwardFastClicks >= 2) {
				if(showUiRunnableFromLeft != null) {
					cancelDelayed(showUiRunnableFromLeft);
					showUiRunnableFromLeft = null;
				}

				binding.doubleTapBackward.setBackgroundResource(R.drawable.ripple_circle_white);
				//player.seekTo(player.getCurrentPosition() - 10_000);
				player.seekBack();
				controller.updateTimers();
			} else {
				showUiRunnableFromLeft = controller::toggleUiVisibility;
				runDelayed(showUiRunnableFromLeft, SHOW_UI_AFTER_MILLIS);
			}

			runDelayed(() -> {
				backwardFastClicks--;

				if(backwardFastClicks == 0) {
					binding.doubleTapBackward.setBackground(null);
				}
			}, 500);
		});

		binding.doubleTapForward.setOnClickListener(view -> {
			if(doubleTapSeek == 0) {
				controller.toggleUiVisibility();
				return;
			}

			forwardFastClicks++;

			if(forwardFastClicks >= 2) {
				if(showUiRunnableFromRight != null) {
					cancelDelayed(showUiRunnableFromRight);
					showUiRunnableFromRight = null;
				}

				binding.doubleTapForward.setBackgroundResource(R.drawable.ripple_circle_white);
				player.seekForward();
				controller.updateTimers();
			} else {
				showUiRunnableFromRight = controller::toggleUiVisibility;
				runDelayed(showUiRunnableFromRight, SHOW_UI_AFTER_MILLIS);
			}

			runDelayed(() -> {
				forwardFastClicks--;

				if(forwardFastClicks == 0) {
					binding.doubleTapForward.setBackground(null);
				}
			}, 500);
		});

		binding.slider.addListener(new TimeBar.OnScrubListener() {
			@Override
			public void onScrubStart(@NonNull TimeBar timeBar, long position) {
				controller.addLockedUiReason("seek");

				player.seekTo(position);
				controller.updateTimers();

				player.pause();
			}

			@Override
			public void onScrubMove(@NonNull TimeBar timeBar, long position) {
				player.seekTo(position);
				controller.updateTimers();
			}

			@Override
			public void onScrubStop(@NonNull TimeBar timeBar, long position, boolean canceled) {
				controller.removeLockedUiReason("seek");

				player.seekTo(position);
				controller.updateTimers();

				if(!isVideoPaused) {
					player.play();
				}
			}
		});

		binding.getRoot().setOnTouchListener((view, event) -> {
			if(event.getAction() == MotionEvent.ACTION_MOVE) return true;
			if(event.getAction() == MotionEvent.ACTION_DOWN) return true;

			controller.toggleUiVisibility();
			return true;
		});

		Runnable updateProgress = new Runnable() {
			@Override
			public void run() {
				if(isDestroyed()) return;
				controller.updateTimers();
				runDelayed(this, 1_000);
			}
		};

		runDelayed(updateProgress, 1_000);
		binding.getRoot().performClick();

		setupButton(binding.exit, this::finish);
		setupButton(binding.settings, controller::openSettingsDialog);

		if(bigSeek > 0) {
			var time = StringUtils.formatTimer(bigSeek * 1000L);
			binding.quickSkip.setText("Skip " + time);

			setupButton(binding.quickSkip, () -> {
				player.seekTo(player.getCurrentPosition() + bigSeek * 1000L);
				controller.updateTimers();
			});
		} else {
			binding.quickSkip.setVisibility(View.GONE);
		}

		setupButton(binding.pause, () -> {
			if(isVideoBuffering) return;

			if(player.isPlaying()) {
				player.pause();
				controller.addLockedUiReason("pause");
			} else {
				player.play();
				controller.removeLockedUiReason("pause");
			}

			isVideoPaused = !player.isPlaying();
		});

		setupPip();
		loadData();
		setButtonsClickability(false);
		controller.showUiTemporarily();
	}

	private void loadSettings() {
		var prefs = AwerySettings.getInstance(this);

		doubleTapSeek = prefs.getInt(AwerySettings.player.DOUBLE_TAP_SEEK_LENGTH);
		bigSeek = prefs.getInt(AwerySettings.player.BIG_SEEK_LENGTH);

		gesturesMode = StringUtils.parseEnum(
				prefs.getString(AwerySettings.player.GESTURES_MODE),
				GesturesMode.VOLUME_BRIGHTNESS);
	}

	@Override
	public void onCues(@NonNull CueGroup cueGroup) {
		binding.subtitleView.setCues(cueGroup.cues);
	}

	@Override
	public void onIsPlayingChanged(boolean isPlaying) {
		var res = isPlaying ? R.drawable.anim_play_to_pause : R.drawable.anim_pause_to_play;
		Glide.with(this).load(res).into(binding.pause);
	}

	public static void selectSource(ExtensionProvider source) {
		PlayerActivity.source = source;
	}

	public void selectSubtitles(@Nullable CatalogSubtitle subtitles) {
		MediaItem.SubtitleConfiguration subtitleItem = null;

		if(subtitles == null) {
			var isEmpty = video.getSubtitles().isEmpty();
			binding.subtitles.setAlpha(isEmpty ? .4f : 1f);
			binding.subtitles.setImageResource(R.drawable.ic_subtitles_outlined);
		} else {
			binding.subtitles.setAlpha(1f);
			binding.subtitles.setImageResource(R.drawable.ic_subtitles_filled);

			subtitleItem = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitles.getUrl()))
					.setMimeType(MimeTypes.TEXT_VTT)
					.setLanguage("en")
					.setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
					.build();
		}

		if(subtitleItem != null) {
			var item = videoItem.buildUpon()
					.setSubtitleConfigurations(List.of(subtitleItem))
					.build();

			player.setMediaItem(item, false);
		} else {
			// Don't know why, but the video doesn't start if we don't reset the position
			// And this is happening only at the first playback
			player.setMediaItem(videoItem, !didSelectedVideo);
		}

		player.play();
	}

	public void playVideo(@NonNull CatalogVideo video) {
		var url = video.getUrl();

		// Handle torrents
		if(url.startsWith("magnet")) {
			var uri = Uri.parse(url);

			try {
				var intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				startActivity(intent);
				finish();
			} catch(ActivityNotFoundException e) {
				new DialogBuilder(this)
						.setTitle("Can't play torrents")
						.setMessage("Sorry, but Awery doesn't support torrents playback at this time. You can try installing a third-party torrent player to play this type of file.")
						.setPositiveButton("Ok", dialog -> {
							dialog.dismiss();
							finish();
						})
						.setNeutralButton("Copy link", dialog -> {
							var clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							var clipData = ClipData.newRawUri(url, uri);
							clipboard.setPrimaryClip(clipData);
						})
						.show();
			}
		}

		this.videoItem = MediaItem.fromUri(url);
		this.video = video;

		selectSubtitles(null);

		if(video.getSubtitles().isEmpty()) {
			binding.subtitles.setAlpha(.35f);
			setupButton(binding.subtitles, () -> {});
		} else {
			binding.subtitles.setAlpha(.6f);
			setupButton(binding.subtitles, controller::openSubtitlesDialog);
		}

		didSelectedVideo = true;
	}

	private void loadData() {
		onPlaybackStateChanged(Player.STATE_BUFFERING);
		var intent = getIntent();

		this.episode = intent.getParcelableExtra("episode");
		this.episodes = intent.getParcelableArrayListExtra("episodes");

		if(episode != null) {
			binding.title.setText(episode.getTitle());

			source.getVideos(episode, new ExtensionProvider.ResponseCallback<>() {
				@Override
				public void onSuccess(List<CatalogVideo> catalogVideos) {
					if(isDestroyed()) return;

					runOnUiThread(() -> {
						episode.setVideos(catalogVideos);
						controller.openQualityDialog(true);
					});
				}

				@Override
				public void onFailure(Throwable throwable) {
					if(isDestroyed()) return;

					var error = new ExceptionDescriptor(throwable);
					Log.e(TAG, "Failed to load videos list!", throwable);

					toast(error.getTitle(PlayerActivity.this), 1);
					finish();
				}
			});
		} else {
			toast("External videos are not supported yet");
			finish();
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	protected PictureInPictureParams getPipParams() {
		var pipParams = new PictureInPictureParams.Builder();
		var format = player.getVideoFormat();

		if(format != null) {
			var ratio = new Rational(format.width, format.height);
			pipParams.setAspectRatio(ratio);
		}

		var pauseAction = !isVideoPaused ? PLAYBACK_ACTION_PAUSE : PLAYBACK_ACTION_RESUME;
		var pauseIcon = !isVideoPaused ? R.drawable.ic_round_pause_24 : R.drawable.ic_play_filled;
		var pauseTitle = !isVideoPaused ? "Pause" : "Resume";

		/*pipParams.setActions(List.of(new RemoteAction(
				Icon.createWithResource(this, pauseIcon), pauseTitle, pauseTitle,
				PendingIntent.getBroadcast(this, 0,
						new Intent(this, BroadcastReceiver.class).putExtra(PLAYBACK_ACTION, pauseAction),
						PendingIntent.FLAG_UPDATE_CURRENT))));*/

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			pipParams.setTitle(video != null ? video.getTitle() : null);
		}

		return pipParams.build();
	}

	private void setupPip() {
		if((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ||
				(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE))) {
			binding.pip.setVisibility(View.GONE);
			return;
		}

		setupButton(binding.pip, () ->
				enterPictureInPictureMode(getPipParams()));
	}

	@Override
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode);

		var uiVisibility = isInPictureInPictureMode ? View.GONE : View.VISIBLE;
		binding.uiOverlay.setVisibility(uiVisibility);
		binding.darkOverlay.setVisibility(uiVisibility);

		player.play();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void setupButton(@NonNull View view, Runnable clickListener) {
		buttons.add(view);

		view.setOnClickListener(v -> {
			if(!areButtonsClickable) {
				return;
			}

			controller.showUiTemporarily();
			clickListener.run();
		});
	}

	public void setButtonsClickability(boolean isClickable) {
		this.areButtonsClickable = isClickable;
		binding.slider.setEnabled(isClickable);

		for(var view : buttons) {
			view.setClickable(isClickable);
		}
	}

	@Override
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

			case Player.STATE_ENDED -> {
				if(!didSelectedVideo) return;
				finish();
			}

			case Player.STATE_IDLE -> {}
		}
	}

	@Override
	public void onPlayerError(@NonNull PlaybackException e) {
		Log.e(TAG, "Player error has occurred", e);

		toast(switch(e.errorCode) {
			case PlaybackException.ERROR_CODE_TIMEOUT -> "Connection timeout has occurred, please try again later";
			case PlaybackException.ERROR_CODE_DECODING_FAILED -> "Video decoding failed, please try again later";
			case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Video not found, please try again later";

			case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
					PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
					"Connection error has occurred, please try again later";

			default -> "Unknown error has occurred, please try again later";
		}, 1);

		controller.openQualityDialog(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		player.prepare();
		player.play();

		AweryApp.addOnBackPressedListener(this, this::finish);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(!isVideoPaused) {
			player.play();
		}

		controller.showUiTemporarily();
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
		session.release();

		player = null;
		source = null;
		session = null;
	}

	private void applyFullscreen() {
		var controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
		controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
		controller.hide(UI_INSETS);

		ViewUtil.setOnApplyInsetsListener(getWindow().getDecorView(), insets -> {
			var systemInsets = insets.getInsets(UI_INSETS);

			ViewUtil.setLeftMargin(binding.exit, systemInsets.left);
			ViewUtil.setLeftMargin(binding.slider, systemInsets.left);
			ViewUtil.setLeftMargin(binding.bottomControls, systemInsets.left);
			ViewUtil.setBottomMargin(binding.slider, systemInsets.bottom);
		});
	}

	public enum GesturesMode {
		VOLUME_BRIGHTNESS, DISABLED
	}
}