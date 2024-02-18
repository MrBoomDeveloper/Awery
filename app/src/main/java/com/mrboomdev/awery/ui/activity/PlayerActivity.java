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

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		ani.awery.databinding.LayoutActivityPlayerBinding binding = LayoutActivityPlayerBinding.inflate(getLayoutInflater());

		var player = new ExoPlayer.Builder(this)
				//.setMediaSourceFactory(null)
				.build();

		player.setVideoTextureView(binding.textureView);

		var url = getIntent()
				.getStringExtra("url");

		var item = MediaItem.fromUri(url);

		player.setMediaItem(item);
		player.prepare();
		player.play();

		AweryApp.setOnBackPressedListener(this, this::finish);
	}
}