package com.mrboomdev.awery.ui.activity.player;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.cancelDelayed;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.util.NiceUtils.formatTimer;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyInsetsListener;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.TimeBar;

import com.bumptech.glide.Glide;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.app.data.settings.SettingsItem;
import com.mrboomdev.awery.app.data.settings.SettingsList;
import com.mrboomdev.awery.databinding.ScreenPlayerBinding;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.data.CatalogVideoFile;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.NiceUtils;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;
import com.mrboomdev.awery.util.extensions.ActivityExtensionsKt;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

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
	private CatalogSubtitle currentSubtitle;
	private PlayerGestures gestures;
	protected ScreenPlayerBinding binding;
	protected Runnable showUiRunnableFromLeft, showUiRunnableFromRight;
	protected boolean areButtonsClickable;
	protected boolean isVideoPaused, isVideoBuffering = true, didSelectedVideo;
	protected int forwardFastClicks, backwardFastClicks;
	protected List<CatalogVideo> episodes;
	protected CatalogVideo episode;
	protected CatalogVideoFile video;
	protected ExoPlayer player;
	protected int doubleTapSeek, bigSeek;
	protected AwerySettings.PlayerGesturesMode_Values gesturesMode;
	private MediaItem videoItem;

	@SuppressLint({"ClickableViewAccessibility", "UnspecifiedRegisterReceiverFlag"})
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		ActivityExtensionsKt.enableEdgeToEdge(this);

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
		setupButton(binding.subtitles, controller::openSubtitlesDialog);

		if(bigSeek > 0) {
			var time = formatTimer(bigSeek * 1000L);
			binding.quickSkip.setText(getString(R.string.skip) + " " + time);

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

		controller.setAspectRatio(Objects.requireNonNull(AwerySettings.VIDEO_ASPECT_RATIO.getValue()));
		controller.showUiTemporarily();
	}

	private void loadSettings() {
		doubleTapSeek = AwerySettings.PLAYER_DOUBLE_TAP_SEEK_LENGTH.getValue();
		bigSeek = AwerySettings.PLAYER_BIG_SEEK_LENGTH.getValue();
		gesturesMode = AwerySettings.PLAYER_GESTURES_MODE.getValue();
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

	// TODO: Replace this lazy temporary long-term solution with something better
	public static void selectSource(ExtensionProvider source) {
		PlayerActivity.source = source;
	}

	public void setSubtitles(@Nullable CatalogSubtitle subtitles) {
		this.currentSubtitle = subtitles;
		MediaItem.SubtitleConfiguration subtitleItem = null;

		if(subtitles == null) {
			var isEmpty = video.getSubtitles().isEmpty();
			binding.subtitles.setAlpha(isEmpty ? .4f : 1f);
			binding.subtitles.setImageResource(R.drawable.ic_subtitles_outlined);
		} else {
			String mimeType;

			try {
				mimeType = NiceUtils.parseMimeType(subtitles.getUri());
			} catch(IllegalArgumentException e) {
				Log.e(TAG, "Unknown subtitles mime type! " + subtitles.getUri(), e);

				CrashHandler.showErrorDialog(this, new CrashHandler.CrashReport.Builder()
						.setTitle(R.string.unknown_file_type)
						.setMessage(R.string.unknown_file_type_description)
						.setPrefix(R.string.please_report_bug_app)
						.setThrowable(e)
						.build());

				return;
			}

			binding.subtitles.setAlpha(1f);
			binding.subtitles.setImageResource(R.drawable.ic_subtitles_filled);

			subtitleItem = new MediaItem.SubtitleConfiguration.Builder(subtitles.getUri())
					.setMimeType(mimeType)
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

	public void setVideo(@NonNull CatalogVideoFile video) {
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
						.setTitle(R.string.torrent_usupported)
						.setCancelable(false)
						.setMessage(R.string.torrent_unsupported_message)
						.setPositiveButton(R.string.ok, dialog -> {
							dialog.dismiss();
							finish();
						})
						.setNeutralButton(R.string.copy, dialog -> App.Companion.copyToClipboard(uri))
						.show();
			}

			return;
		}

		this.videoItem = MediaItem.fromUri(url);
		this.video = video;

		setSubtitles(currentSubtitle);
		didSelectedVideo = true;
	}

	@SuppressWarnings("unchecked")
	private void loadData() {
		onPlaybackStateChanged(Player.STATE_BUFFERING);
		binding.loadingStatus.setText("Loading videos list...");
		var intent = getIntent();

		this.episode = (CatalogVideo) intent.getSerializableExtra("episode");
		this.episodes = (List<CatalogVideo>) intent.getSerializableExtra("episodes");

		if(episode != null) {
			binding.title.setText(episode.getTitle());

			source.getVideoFiles(new SettingsList(
					new SettingsItem(ExtensionProvider.FILTER_EPISODE, episode)
			)).addCallback(new AsyncFuture.Callback<>() {
				@Override
				public void onSuccess(List<CatalogVideoFile> catalogVideos) {
					if(isDestroyed()) return;

					runOnUiThread(() -> {
						if(catalogVideos.size() == 1) {
							setVideo(catalogVideos.get(0));
							return;
						}

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

	private void setupButton(@NonNull View view, Runnable clickListener) {
		buttons.add(view);

		view.setOnClickListener(v -> {
			if(!areButtonsClickable) {
				return;
			}

			controller.showUiTemporarily();

			if(clickListener != null) {
				clickListener.run();
			}
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
				binding.loadingStatus.setVisibility(View.GONE);
				binding.pause.setVisibility(View.VISIBLE);
			}

			case Player.STATE_BUFFERING -> {
				isVideoBuffering = true;

				binding.loadingStatus.setText("Buffering video...");

				binding.loadingCircle.setVisibility(View.VISIBLE);
				binding.loadingStatus.setVisibility(View.VISIBLE);
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
			case PlaybackException.ERROR_CODE_TIMEOUT -> getString(R.string.connection_timeout);
			case PlaybackException.ERROR_CODE_DECODING_FAILED -> "Video decoding failed, please try again later";
			case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Video not found, please try again later";

			case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
					PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> getString(R.string.connection_error);

			default -> getString(R.string.unknown_error);
		}, 1);

		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		player.prepare();
		player.play();

		App.addOnBackPressedListener(this, this::finish);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(!isVideoPaused) {
			player.play();
		}

		binding.subtitleView.setUserDefaultStyle();
		binding.subtitleView.setUserDefaultTextSize();

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

		setOnApplyInsetsListener(getWindow().getDecorView(), insets -> {
			var systemInsets = insets.getInsets(UI_INSETS);

			setLeftMargin(binding.exit, systemInsets.left);
			setLeftMargin(binding.slider, systemInsets.left);
			setLeftMargin(binding.bottomControls, systemInsets.left);
			setBottomMargin(binding.slider, systemInsets.bottom);

			return false;
		});
	}
}