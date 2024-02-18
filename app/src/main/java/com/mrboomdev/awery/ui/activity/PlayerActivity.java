package com.mrboomdev.awery.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.ui.ThemeManager;

import ani.awery.databinding.LayoutActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {
	private ExoPlayer player;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		var binding = LayoutActivityPlayerBinding.inflate(getLayoutInflater());

		player = new ExoPlayer.Builder(this).build();
		binding.player.setPlayer(player);

		var url = getIntent().getStringExtra("url");
		var item = MediaItem.fromUri(url);
		player.setMediaItem(item);

		AweryApp.setOnBackPressedListener(this, this::finish);
	}

	@Override
	protected void onStart() {
		super.onStart();
		player.prepare();
		player.setPlayWhenReady(true);
	}
}