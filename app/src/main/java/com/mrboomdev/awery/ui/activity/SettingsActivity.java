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
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.SettingsFactory;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.adapter.SettingsAdapter;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import ani.awery.R;
import ani.awery.databinding.LayoutActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.DataHandler {
	public RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
	private AwerySettings settings;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		var itemJson = getIntent().getStringExtra("item");
		settings = AwerySettings.getInstance(this);
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
		var recyclerAdapter = new SettingsAdapter(item, this);

		var binding = LayoutActivitySettingsBinding.inflate(getLayoutInflater());
		binding.recycler.setRecycledViewPool(viewPool);
		binding.recycler.setAdapter(recyclerAdapter);

		ViewUtil.setOnApplyUiInsetsListener(binding.getRoot(), insets ->
				ViewUtil.setTopPadding(binding.recycler, insets.top + ViewUtil.dpPx(12)), frame);

		frame.addView(binding.getRoot());
	}

	@Override
	public void onScreenLaunchRequest(@NonNull SettingsItem item) {
		item.restoreValues();

		var moshi = new Moshi.Builder().add(new SettingsItem.Adapter()).build();
		var jsonAdapter = moshi.adapter(SettingsItem.class);

		var intent = new Intent(SettingsActivity.this, SettingsActivity.class);
		intent.putExtra("item", jsonAdapter.toJson(item));
		startActivity(intent);
	}

	@Override
	public void save(@NonNull SettingsItem item, Object newValue) {
		switch(item.getType()) {
			case BOOLEAN -> settings.setBoolean(item.getFullKey(), (boolean) newValue).saveAsync();
			case SELECT -> settings.setString(item.getFullKey(), (String) newValue).saveAsync();
		}
	}
}