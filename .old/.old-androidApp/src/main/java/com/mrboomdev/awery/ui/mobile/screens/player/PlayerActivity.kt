package com.mrboomdev.awery.ui.mobile.screens.player

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.TimeBar
import androidx.media3.ui.TimeBar.OnScrubListener
import com.bumptech.glide.Glide
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.copyToClipboard
import com.mrboomdev.awery.app.AweryLifecycle.Companion.cancelDelayed
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runDelayed
import com.mrboomdev.awery.app.CrashHandler
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.data.settings.SettingsItem
import com.mrboomdev.awery.data.settings.SettingsList
import com.mrboomdev.awery.databinding.ScreenPlayerBinding
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.extensions.data.CatalogSubtitle
import com.mrboomdev.awery.extensions.data.CatalogVideo
import com.mrboomdev.awery.extensions.data.CatalogVideoFile
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.Platform.toast
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.util.NiceUtils
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.exceptions.explain
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.bottomMargin
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.leftMargin
import com.mrboomdev.awery.util.extensions.toMimeType
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import com.mrboomdev.safeargsnext.util.rememberSafeArgs

private const val TAG = "PlayerActivity"
private const val SHOW_UI_AFTER_MILLIS = 200

private val PLAYER_UI_INSETS = (WindowInsetsCompat.Type.displayCutout()
		or WindowInsetsCompat.Type.systemGestures()
		or WindowInsetsCompat.Type.statusBars()
		or WindowInsetsCompat.Type.navigationBars())

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity(), SafeArgsActivity<PlayerActivity.Extras>, Player.Listener {
	class Extras(
		val source: String,
		val episode: CatalogVideo,
		val episodes: List<CatalogVideo>
	)

	private val buttons: MutableSet<View> = HashSet()
	private val controller = PlayerController(this)
	private var session: MediaSession? = null
	private var currentSubtitle: CatalogSubtitle? = null
	private lateinit var gestures: PlayerGestures
	lateinit var binding: ScreenPlayerBinding
	private var showUiRunnableFromLeft: Runnable? = null
	private var showUiRunnableFromRight: Runnable? = null
	private var areButtonsClickable = false
	private var isVideoPaused = false
	private var isVideoBuffering = true
	private var didSelectedVideo = false
	private var forwardFastClicks = 0
	private var backwardFastClicks = 0
	@JvmField
	var episode: CatalogVideo? = null
	@JvmField
	var video: CatalogVideoFile? = null
	@JvmField
	var player: ExoPlayer? = null
	private var doubleTapSeek by AwerySettings.PLAYER_DOUBLE_TAP_SEEK_LENGTH
	private var bigSeek = 0
	@JvmField
	var gesturesMode: AwerySettings.PlayerGesturesModeValue? = null
	private var videoItem: MediaItem? = null

	@SuppressLint("ClickableViewAccessibility", "UnspecifiedRegisterReceiverFlag")
	override fun onCreate(savedInstanceState: Bundle?) {
		applyTheme()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		PlayerPip.addCallback(this) { action ->
			when(action) {
				PlayerPip.Action.PLAY -> {
					player?.play()
					isVideoPaused = false
				}

				PlayerPip.Action.PAUSE -> {
					player?.pause()
					isVideoPaused = true
				}
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				try {
					setPictureInPictureParams(pipParams)
				} catch(e: RuntimeException) {
					Log.e(TAG, "Failed to update picture in picture params", e)
				}
			}
		}

		binding = ScreenPlayerBinding.inflate(layoutInflater)
		setContentView(binding.root)
		applyFullscreen()

		gestures = PlayerGestures(this)
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

		binding.aspectRatioFrame.setAspectRatio(16f / 9f)
		binding.aspectRatioFrame.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

		val audioAttributes = AudioAttributes.Builder()
			.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
			.setUsage(C.USAGE_MEDIA)
			.build()

		player = ExoPlayer.Builder(this)
			.setSeekBackIncrementMs(doubleTapSeek.key.toInt() * 1000L + 1)
			.setSeekForwardIncrementMs(doubleTapSeek.key.toInt() * 1000L + 1)
			.setAudioAttributes(audioAttributes, true)
			.build().apply {
				setVideoSurfaceView(binding.surfaceView)
				setHandleAudioBecomingNoisy(true)
				addListener(this@PlayerActivity)
			}

		session = MediaSession.Builder(this, player!!)
			.setCallback(object : MediaSession.Callback {
				override fun onConnect(
					session: MediaSession,
					controller: MediaSession.ControllerInfo
				): ConnectionResult {
					return AcceptedResultBuilder(session)
						.setAvailableSessionCommands(ConnectionResult.DEFAULT_SESSION_COMMANDS)
						.build()
				}
			}).build()

		binding.doubleTapBackward.setOnTouchListener { _, event -> gestures.onTouchEventLeft(event) }
		binding.doubleTapForward.setOnTouchListener { _, event -> gestures.onTouchEventRight(event) }

		binding.doubleTapBackward.setOnClickListener {
			if(doubleTapSeek.key.toInt() == 0) {
				controller.toggleUiVisibility()
				return@setOnClickListener
			}

			backwardFastClicks++

			if(backwardFastClicks >= 2) {
				if(showUiRunnableFromLeft != null) {
					cancelDelayed(showUiRunnableFromLeft)
					showUiRunnableFromLeft = null
				}

				binding.doubleTapBackward.setBackgroundResource(R.drawable.ripple_circle_white)
				//player.seekTo(player.getCurrentPosition() - 10_000);
				player?.seekBack()
				controller.updateTimers()
			} else {
				showUiRunnableFromLeft = Runnable { controller.toggleUiVisibility() }
				runDelayed(showUiRunnableFromLeft, SHOW_UI_AFTER_MILLIS.toLong())
			}

			runDelayed({
				backwardFastClicks--
				if(backwardFastClicks == 0) {
					binding.doubleTapBackward.background = null
				}
			}, 500)
		}

		binding.doubleTapForward.setOnClickListener {
			if(doubleTapSeek.key.toInt() == 0) {
				controller.toggleUiVisibility()
				return@setOnClickListener
			}

			forwardFastClicks++

			if(forwardFastClicks >= 2) {
				if(showUiRunnableFromRight != null) {
					cancelDelayed(showUiRunnableFromRight)
					showUiRunnableFromRight = null
				}

				binding.doubleTapForward.setBackgroundResource(R.drawable.ripple_circle_white)
				player?.seekForward()
				controller.updateTimers()
			} else {
				showUiRunnableFromRight = Runnable { controller.toggleUiVisibility() }
				runDelayed(showUiRunnableFromRight, SHOW_UI_AFTER_MILLIS.toLong())
			}

			runDelayed({
				forwardFastClicks--
				if(forwardFastClicks == 0) {
					binding.doubleTapForward.background = null
				}
			}, 500)
		}

		binding.slider.addListener(object : OnScrubListener {
			override fun onScrubStart(timeBar: TimeBar, position: Long) {
				controller.addLockedUiReason("seek")

				player?.seekTo(position)
				controller.updateTimers()

				player?.pause()
			}

			override fun onScrubMove(timeBar: TimeBar, position: Long) {
				player?.seekTo(position)
				controller.updateTimers()
			}

			override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
				controller.removeLockedUiReason("seek")

				player?.seekTo(position)
				controller.updateTimers()

				if(!isVideoPaused) {
					player?.play()
				}
			}
		})

		binding.root.setOnTouchListener { _, event ->
			if(event.action == MotionEvent.ACTION_MOVE) return@setOnTouchListener true
			if(event.action == MotionEvent.ACTION_DOWN) return@setOnTouchListener true

			controller.toggleUiVisibility()
			true
		}

		val updateProgress: Runnable = object : Runnable {
			override fun run() {
				if(isDestroyed) return
				controller.updateTimers()
				runDelayed(this, 1000)
			}
		}

		runDelayed(updateProgress, 1000)
		binding.root.performClick()

		setupButton(binding.exit) { this.finish() }
		setupButton(binding.settings) { controller.openSettingsDialog() }
		setupButton(binding.subtitles) { controller.openSubtitlesDialog() }

		if(bigSeek > 0) {
			val time = NiceUtils.formatTimer(bigSeek * 1000L)
			binding.quickSkip.text = "${i18n(Res.string.skip)} $time"

			setupButton(binding.quickSkip) {
				player?.seekTo(player!!.currentPosition + bigSeek * 1000L)
				controller.updateTimers()
			}
		} else {
			binding.quickSkip.visibility = View.GONE
		}

		setupButton(binding.pause) {
			if(isVideoBuffering) return@setupButton
			if(player!!.isPlaying) {
				player!!.pause()
				controller.addLockedUiReason("pause")
			} else {
				player!!.play()
				controller.removeLockedUiReason("pause")
			}

			isVideoPaused = !player!!.isPlaying
		}

		setupPip()
		loadData()
		setButtonsClickability(false)

		controller.setAspectRatio(AwerySettings.VIDEO_ASPECT_RATIO.value)
		controller.showUiTemporarily()

		addOnPictureInPictureModeChangedListener {
			if(!it.isInPictureInPictureMode && !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
				// User has pressed an "Close" button and the player isn't visible
				// nor fullscreen nor in the minimized state.
				finish()
				return@addOnPictureInPictureModeChangedListener
			}

			val uiVisibility = if(it.isInPictureInPictureMode) View.GONE else View.VISIBLE
			binding.uiOverlay.visibility = uiVisibility
			binding.darkOverlay.visibility = uiVisibility

			player?.play()
		}
	}

	override fun onPictureInPictureRequested(): Boolean {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && AwerySettings.PIP_ON_BACKGROUND.value == true) {
			enterPictureInPictureMode(pipParams)
		}

		return true
	}

	override fun onCues(cueGroup: CueGroup) {
		binding.subtitleView.setCues(cueGroup.cues)
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		if(lifecycle.currentState == Lifecycle.State.DESTROYED) return
		val res = if(isPlaying) R.drawable.anim_play_to_pause else R.drawable.anim_pause_to_play
		Glide.with(this).load(res).into(binding.pause)
	}

	fun setSubtitles(subtitles: CatalogSubtitle?) {
		this.currentSubtitle = subtitles
		var subtitleItem: SubtitleConfiguration? = null

		if(subtitles == null) {
			val isEmpty = video!!.subtitles.isEmpty()
			binding.subtitles.alpha = if(isEmpty) .4f else 1f
			binding.subtitles.setImageResource(R.drawable.ic_subtitles_outlined)
		} else {
			val mimeType = try {
				subtitles.uri.toMimeType()
			} catch(e: IllegalArgumentException) {
				Log.e(TAG, "Unknown subtitles mime type! " + subtitles.uri, e)

				CrashHandler.showDialog(
					context = this,
					title = i18n(Res.string.unknown_file_type),
					message = i18n(Res.string.unknown_file_type_description),
					messagePrefix = i18n(Res.string.please_report_bug_app),
					throwable = e
				)

				return
			}

			binding.subtitles.alpha = 1f
			binding.subtitles.setImageResource(R.drawable.ic_subtitles_filled)

			subtitleItem = SubtitleConfiguration.Builder(subtitles.uri)
				.setMimeType(mimeType)
				.setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
				.build()
		}

		if(subtitleItem != null) {
			val item = videoItem!!.buildUpon()
				.setSubtitleConfigurations(listOf(subtitleItem))
				.build()

			player?.setMediaItem(item, false)
		} else {
			// Don't know why, but the video doesn't start if we don't reset the position
			// And this is happening only at the first playback
			player?.setMediaItem(videoItem!!, !didSelectedVideo)
		}

		player?.play()
	}

	fun setVideo(video: CatalogVideoFile) {
		val url = video.url

		// Handle torrents
		if(url.startsWith("magnet")) {
			val uri = Uri.parse(url)

			try {
				val intent = Intent(Intent.ACTION_VIEW)
				intent.setData(uri)
				startActivity(intent)
				finish()
			} catch(e: ActivityNotFoundException) {
				DialogBuilder(this)
					.setTitle(i18n(Res.string.torrent_usupported))
					.setCancelable(false)
					.setMessage(i18n(Res.string.torrent_unsupported_message))
					.setPositiveButton(i18n(Res.string.ok)) { dialog ->
						dialog.dismiss()
						finish()
					}
					.setNeutralButton(i18n(Res.string.copy)) { copyToClipboard(uri.toString()) }
					.show()
			}

			return
		}

		this.videoItem = MediaItem.fromUri(url)
		this.video = video

		setSubtitles(currentSubtitle)
		didSelectedVideo = true
	}

	private fun loadData() {
		onPlaybackStateChanged(Player.STATE_BUFFERING)
		binding.loadingStatus.text = i18n(Res.string.loading_videos_list)

		this.episode = rememberSafeArgs!!.episode

		if(episode != null) {
			binding.title.text = episode!!.title

			ExtensionProvider.forGlobalId(rememberSafeArgs!!.source).getVideoFiles(
				SettingsList(SettingsItem(ExtensionProvider.FILTER_EPISODE, episode))
			).addCallback(object : AsyncFuture.Callback<List<CatalogVideoFile>?> {
				override fun onSuccess(catalogVideos: List<CatalogVideoFile>) {
					if(isDestroyed) return

					runOnUiThread {
						if(catalogVideos.size == 1) {
							setVideo(catalogVideos[0])
							return@runOnUiThread
						}
						episode!!.videos = catalogVideos
						controller.openQualityDialog(true)
					}
				}

				override fun onFailure(throwable: Throwable) {
					if(isDestroyed) return

					Log.e(TAG, "Failed to load videos list!", throwable)
					toast(throwable.explain().title, 1)
					finish()
				}
			})
		} else {
			toast("External videos are not supported yet")
			finish()
		}
	}

	@get:RequiresApi(api = Build.VERSION_CODES.O)
	private val pipParams: PictureInPictureParams
		get() = PictureInPictureParams.Builder().apply {
			player?.videoFormat?.also {
				setAspectRatio(Rational(it.width, it.height))
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				setTitle(if(video != null) video!!.title else null)
			}

			val pauseTitle = if(!isVideoPaused) "Pause" else "Resume"

			setActions(
				listOf(
					RemoteAction(
						(if(!isVideoPaused) R.drawable.ic_round_pause_24 else R.drawable.ic_play_filled).let { res ->
							Icon.createWithResource(this@PlayerActivity, res)
						},

						pauseTitle,
						pauseTitle,

						PendingIntent.getBroadcast(
							this@PlayerActivity,
							0,
							Intent(this@PlayerActivity, PlayerPip.Receiver::class.java).apply {
								putExtra(PlayerPip.ACTION, (if(isVideoPaused) PlayerPip.Action.PLAY else PlayerPip.Action.PAUSE).name)
							}, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
						)
					)
				)
			)
		}.build()

	private fun setupPip() {
		if((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) ||
			(!packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE))
		) {
			binding.pip.visibility = View.GONE
			return
		}

		setupButton(binding.pip) { enterPictureInPictureMode(pipParams) }
	}

	private fun setupButton(view: View, clickListener: Runnable?) {
		buttons.add(view)

		view.setOnClickListener {
			if(!areButtonsClickable) {
				return@setOnClickListener
			}

			controller.showUiTemporarily()
			clickListener?.run()
		}
	}

	fun setButtonsClickability(isClickable: Boolean) {
		this.areButtonsClickable = isClickable
		binding.slider.isEnabled = isClickable

		for(view in buttons) {
			view.isClickable = isClickable
		}
	}

	override fun onPlaybackStateChanged(playbackState: Int) {
		when(playbackState) {
			Player.STATE_READY -> {
				isVideoBuffering = false

				binding.slider.setDuration(player!!.duration)

				binding.loadingCircle.visibility = View.GONE
				binding.loadingStatus.visibility = View.GONE
				binding.pause.visibility = View.VISIBLE
			}

			Player.STATE_BUFFERING -> {
				isVideoBuffering = true

				binding.loadingStatus.text = i18n(Res.string.buffering_video)

				binding.loadingCircle.visibility = View.VISIBLE
				binding.loadingStatus.visibility = View.VISIBLE
				binding.pause.visibility = View.INVISIBLE
			}

			Player.STATE_ENDED -> {
				if(!didSelectedVideo) return
				finish()
			}

			Player.STATE_IDLE -> {}
		}
	}

	override fun onPlayerError(e: PlaybackException) {
		Log.e(TAG, "Player error has occurred", e)

		toast(when(e.errorCode) {
			PlaybackException.ERROR_CODE_TIMEOUT -> i18n(Res.string.connection_timeout)
			PlaybackException.ERROR_CODE_DECODING_FAILED -> "Video decoding failed, please try again later"
			PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Video not found, please try again later"
			PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> i18n(Res.string.connection_error)
			else -> i18n(Res.string.unknown_error) + " (${e.errorCode})"
		}, 1)

		finish()
	}

	override fun onStart() {
		super.onStart()
		player?.prepare()
		player?.play()
	}

	override fun onResume() {
		super.onResume()

		if(!isVideoPaused) {
			player?.play()
		}

		binding.subtitleView.setUserDefaultStyle()
		binding.subtitleView.setUserDefaultTextSize()

		controller.showUiTemporarily()
	}

	override fun onPause() {
		super.onPause()
		player?.pause()
	}

	override fun onDestroy() {
		super.onDestroy()

		player?.stop()
		player?.release()
		session?.release()

		player = null
		session = null
	}

	private fun applyFullscreen() {
		WindowCompat.getInsetsController(window, window.decorView).apply {
			systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			hide(PLAYER_UI_INSETS)
		}

		window.decorView.applyInsets(PLAYER_UI_INSETS, { _, insets ->
			binding.exit.leftMargin = insets.left
			binding.slider.leftMargin = insets.left
			binding.bottomControls.leftMargin = insets.left
			binding.slider.bottomMargin = insets.bottom
			false
		})
	}
}