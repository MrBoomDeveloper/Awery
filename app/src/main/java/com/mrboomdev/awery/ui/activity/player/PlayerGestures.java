package com.mrboomdev.awery.ui.activity.player;

import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public class PlayerGestures {
	private static final int GESTURE_START_Y = 150;
	private static final String TAG = "PlayerGestures";
	private final AudioManager audioManager;
	private final PlayerActivity activity;
	private float brightness, volume;
	private final int maxVolume;
	private float startLeftY, startRightY;
	private boolean isLeftDown, isRightDown,
			isLeftTracking, isRightTracking,
			isLeftIgnored, isRightIgnored;

	public PlayerGestures(@NonNull PlayerActivity activity) {
		this.activity = activity;
		this.audioManager = activity.getSystemService(AudioManager.class);
		this.maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	public boolean onTouchEventRight(@NonNull MotionEvent event) {
		return switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN -> {
				if(isRightDown) {
					yield false;
				}

				if(event.getY() < GESTURE_START_Y || activity.gesturesMode != PlayerActivity.GesturesMode.VOLUME_BRIGHTNESS) {
					isRightIgnored = true;
					yield false;
				}

				isRightDown = true;
				startRightY = event.getY();
				yield false;
			}

			case MotionEvent.ACTION_MOVE -> {
				if(isRightIgnored) yield false;

				if(!isRightTracking) {
					if(Math.abs(event.getY() - startRightY) > 15) {
						isRightTracking = true;
					} else {
						yield false;
					}

					volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					activity.binding.volumeGesture.setMaxProgress(maxVolume);
					activity.binding.volumeGesture.setIsVisible(true);
					activity.binding.volumeGesture.setProgress((int) volume, false);
					isRightTracking = true;
				}

				var delta = event.getY() - startRightY;
				var volume = this.volume - (delta / 18f);

				if(volume > maxVolume) volume = maxVolume;
				if(volume < 0) volume = 0;

				activity.binding.volumeGesture.setProgress((int) volume, false);
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) volume, 0);

				yield true;
			}

			case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
				var wasTracking = isRightTracking;

				isRightDown = false;
				isRightTracking = false;
				isRightIgnored = false;

				activity.binding.volumeGesture.setIsVisible(false);

				yield wasTracking;
			}

			default -> false;
		};
	}

	public boolean onTouchEventLeft(@NonNull MotionEvent event) {
		return switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN -> {
				if(isLeftDown) {
					yield false;
				}

				if(event.getY() < GESTURE_START_Y || activity.gesturesMode != PlayerActivity.GesturesMode.VOLUME_BRIGHTNESS) {
					isLeftIgnored = true;
					yield false;
				}

				isLeftDown = true;
				startLeftY = event.getY();
				yield false;
			}

			case MotionEvent.ACTION_MOVE -> {
				if(isLeftIgnored) yield false;

				if(!isLeftTracking) {
					if(Math.abs(event.getY() - startLeftY) > 15) {
						isLeftTracking = true;
					} else {
						yield false;
					}

					try {
						var setting = Settings.System.SCREEN_BRIGHTNESS;
						var resolver = activity.getContentResolver();

						brightness = Settings.System.getInt(resolver, setting) / 255f;
					} catch(Settings.SettingNotFoundException e) {
						brightness = 1;
						Log.e(TAG, "Brightness setting not found", e);
					}

					activity.binding.brightnessGesture.setProgress((int) (brightness * 25), false);
					activity.binding.brightnessGesture.setIsVisible(true);
					isLeftTracking = true;
				}

				var delta = event.getY() - startLeftY;
				var brightness = this.brightness - (delta / 250f);

				if(brightness > 1) brightness = 1;
				if(brightness < 0) brightness = 0;

				activity.binding.brightnessGesture.setProgress((int) (brightness * 25), false);

				var params = activity.getWindow().getAttributes();
				params.screenBrightness = brightness;
				activity.getWindow().setAttributes(params);

				yield true;
			}

			case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
				var wasTracking = isLeftTracking;

				isLeftDown = false;
				isLeftTracking = false;
				isLeftIgnored = false;

				activity.binding.brightnessGesture.setIsVisible(false);

				yield wasTracking;
			}

			default -> false;
		};
	}
}