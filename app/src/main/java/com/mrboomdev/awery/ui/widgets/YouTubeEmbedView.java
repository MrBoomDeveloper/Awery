package com.mrboomdev.awery.ui.widgets;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.NiceUtils.cleanUrl;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.io.HttpCacheMode;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpRequest;
import com.mrboomdev.awery.util.io.HttpResponse;

import java.io.IOException;

public class YouTubeEmbedView extends FrameLayout {
	private static final String TAG = "YouTubeEmbedView";
	private static final String PIPED_INSTANCE = "https://pipedapi.kavin.rocks";
	private static final String VIDEO_QUERY_URL = PIPED_INSTANCE + "/streams/";
	private static final String YOUTUBE_SHORT_DOMAIN = "youtu.be";
	private static final String YOUTUBE_DOMAIN = "youtube.com";
	private static final String YOUTUBE_MOBILE_SUBDOMAIN = "m.";
	private static final String WWW = "www.";
	private static final int CACHE_DURATION = 60 * 60 * 1000;
	private final ExoPlayer player;

	@OptIn(markerClass = UnstableApi.class)
	public YouTubeEmbedView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		setBackgroundColor(Color.BLACK);

		var ratioView = new AspectRatioFrameLayout(context);
		addView(ratioView, MATCH_PARENT, MATCH_PARENT);

		var surfaceView = new SurfaceView(context);
		ratioView.addView(surfaceView);

		player = new ExoPlayer.Builder(context).build();
		player.setVideoSurfaceView(surfaceView);
	}

	public YouTubeEmbedView(@NonNull Context context) {
		this(context, null, 0, 0);
	}

	public void loadById(String id) {
		var request = new HttpRequest();
		request.setUrl(VIDEO_QUERY_URL + id);
		request.setCache(HttpCacheMode.CACHE_FIRST, CACHE_DURATION);

		HttpClient.fetch(request).addCallback(new AsyncFuture.Callback<>() {
			@Override
			public void onSuccess(HttpResponse result) {
				try {
					var video = Parser.fromString(PipedResponse.class, result.getText());

					if(video.error != null) {
						showError();
						return;
					}

					player.setMediaItem(new MediaItem.Builder().setUri(video.hls).build());
					player.setPlayWhenReady(true);
					showInfo();
				} catch(IOException e) {
					onFailure(e);
				}
			}

			@Override
			public void onFailure(Throwable t) {
				Log.e(TAG, "Failed to fetch video info!", t);
				runOnUiThread(() -> showError());
			}
		});
	}

	public void loadByUrl(@NonNull String url) {
		url = cleanUrl(url);

		if(url.contains("://")) {
			url = url.substring(url.indexOf("://") + 3);
		}

		if(url.startsWith(WWW)) {
			url = url.substring(WWW.length());
		}

		if(url.startsWith(YOUTUBE_MOBILE_SUBDOMAIN)) {
			url = url.substring(YOUTUBE_MOBILE_SUBDOMAIN.length());
		}

		if(url.startsWith(YOUTUBE_SHORT_DOMAIN)) {
			url = url.substring(YOUTUBE_SHORT_DOMAIN.length());
		}

		if(url.startsWith(YOUTUBE_DOMAIN)) {
			url = url.substring(YOUTUBE_DOMAIN.length());
		}

		if(url.startsWith("/")) url = url.substring(1);
		if(url.contains("?")) url = url.substring(0, url.indexOf("?"));

		loadById(url);
	}

	private void showPreview() {

	}

	private void showError() {
		toast("Failed to load an YouTube video!");
	}

	private void showInfo() {

	}

	public static class PipedResponse {
		public String title, description, uploadDate, visibility;
		public String uploader, uploaderUrl, uploaderAvatar;
		public String thumbnailUrl, hls;
		public String error;
	}
}