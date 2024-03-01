package com.mrboomdev.awery.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.util.HttpClient;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class YouTubeEmbedView extends FrameLayout {
	private final String PIPED_INSTANCE = "https://pipedapi.kavin.rocks";
	private final String VIDEO_QUERY_URL = PIPED_INSTANCE + "/streams/";
	private final String YOUTUBE_SHORT_DOMAIN = "youtu.be";
	private final String YOUTUBE_DOMAIN = "youtube.com";
	private final String YOUTUBE_MOBILE_SUBDOMAIN = "m.";
	private final String WWW = "www.";

	public YouTubeEmbedView(@NonNull Context context) {
		super(context);

		setBackgroundColor(Color.BLACK);
		createLoadingUi();
	}

	public void loadById(String id) {
		HttpClient.get(VIDEO_QUERY_URL + id, (HttpClient.SimpleHttpCallback) (response, exception) -> {
			if(response != null) {
				var moshi = new Moshi.Builder().build();
				var adapter = moshi.adapter(PipedResponse.class);

				try {
					var video = adapter.fromJson(response.getText());

					if(video.error != null) {
						createErrorUi();
						return;
					}

					createInfoUi();
				} catch(IOException e) {
					e.printStackTrace();
				}
			} else if(exception != null) {
				createErrorUi();
				exception.printStackTrace();
			}
		});
	}

	public void loadByUrl(@NonNull String url) {
		if(url.contains("://")) url = url.substring(url.indexOf("://") + 3);

		if(url.startsWith(WWW)) url = url.substring(WWW.length());
		if(url.startsWith(YOUTUBE_MOBILE_SUBDOMAIN)) url = url.substring(YOUTUBE_MOBILE_SUBDOMAIN.length());

		if(url.startsWith(YOUTUBE_SHORT_DOMAIN)) url = url.substring(YOUTUBE_SHORT_DOMAIN.length());
		if(url.startsWith(YOUTUBE_DOMAIN)) url = url.substring(YOUTUBE_DOMAIN.length());

		if(url.startsWith("/")) url = url.substring(1);
		if(url.contains("?")) url = url.substring(0, url.indexOf("?"));

		loadById(url);
	}

	private void createLoadingUi() {
		removeAllViews();
	}

	private void createErrorUi() {
		removeAllViews();
	}

	private void createInfoUi() {
		removeAllViews();
	}

	public static class PipedResponse {
		public String title, description, uploadDate, visibility;
		public String uploader, uploaderUrl, uploaderAvatar;
		public String thumbnailUrl, hls;
		public String error;
	}
}