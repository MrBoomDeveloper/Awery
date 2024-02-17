package com.mrboomdev.awery.ui.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.mrboomdev.awery.catalog.template.CatalogVideo;
import com.squareup.moshi.Moshi;

public class PlayerActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		var moshi = new Moshi.Builder().build();
		var videoAdapter = moshi.adapter(CatalogVideo.class);

		var player = new ExoPlayer.Builder(this).build();

		var item = MediaItem.fromUri("");
		player.setMediaItem(item);
		player.prepare();
		player.play();
	}
}