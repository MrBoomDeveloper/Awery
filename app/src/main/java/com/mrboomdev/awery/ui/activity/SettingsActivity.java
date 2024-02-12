package com.mrboomdev.awery.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.data.settings.SettingsFactory;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.adapter.SettingsAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import ani.awery.databinding.SettingsActivityLayoutBinding;

public class SettingsActivity extends AppCompatActivity {
	public RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		var itemJson = getIntent().getStringExtra("item");
		SettingsItem item;

		if(itemJson == null) {
			item = SettingsFactory.getInstance(this);
		} else {
			var moshi = new Moshi.Builder().build();
			var adapter = moshi.adapter(SettingsItem.class);

			try {
				item = adapter.fromJson(itemJson);
			} catch(IOException e) {
				e.printStackTrace();
				AweryApp.toast(this, "Failed to get settings", 0);
				finish();
				return;
			}
		}

		var frame = new FrameLayout(this);
		createView(item, viewPool, frame);
		setContentView(frame);

		AweryApp.setOnBackPressedListener(this, this::finish);
	}

	@NonNull
	private void createView(
			SettingsItem item,
			RecyclerView.RecycledViewPool viewPool,
			@NonNull FrameLayout frame
	) {
		var recyclerAdapter = new SettingsAdapter(item);

		var binding = SettingsActivityLayoutBinding.inflate(getLayoutInflater());
		binding.recycler.setRecycledViewPool(viewPool);
		binding.recycler.setAdapter(recyclerAdapter);

		recyclerAdapter.setScreenRequestListener(_item -> {
			var moshi = new Moshi.Builder().build();
			var jsonAdapter = moshi.adapter(SettingsItem.class);

			var intent = new Intent(this, SettingsActivity.class);
			intent.putExtra("item", jsonAdapter.toJson(_item));
			startActivity(intent);
		});

		ViewUtil.setOnApplyUiInsetsListener(binding.getRoot(), insets ->
				ViewUtil.setTopPadding(binding.recycler, insets.top + ViewUtil.dpPx(12)), frame);

		frame.addView(binding.getRoot());
	}
}