package com.mrboomdev.awery.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.ui.ThemeManager;

import ani.awery.databinding.LayoutActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {
	private ExoPlayer player;

	@Override
	@OptIn(markerClass = UnstableApi.class)
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);

		super.onCreate(savedInstanceState);
		var binding = LayoutActivityPlayerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.aspectRatioFrame.setAspectRatio(16f / 9f);
		binding.aspectRatioFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

		var url = getIntent().getStringExtra("url");
		var item = MediaItem.fromUri(url);

		player = new ExoPlayer.Builder(this).build();
		player.setVideoTextureView(binding.textureView);
		player.setMediaItem(item);

		AweryApp.setOnBackPressedListener(this, this::finish);
	}

	@Override
	protected void onStart() {
		super.onStart();
		player.prepare();
		player.play();
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