package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.databinding.LayoutHeaderSettingsBinding;
import com.mrboomdev.awery.databinding.ScreenSettingsBinding;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.adapter.SingleViewAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.DataHandler {
	private static final String TAG = "SettingsActivity";
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
			item = AwerySettings.getSettingsMap(this);
		} else {
			var moshi = new Moshi.Builder().build();
			var adapter = moshi.adapter(SettingsItem.class);

			try {
				item = adapter.fromJson(itemJson);
				if(item == null) throw new IllegalArgumentException("Failed to parse settings");
			} catch(IOException e) {
				Log.e(TAG, "Failed to parse settings", e);
				toast(this, "Failed to get settings", 0);
				finish();
				return;
			}
		}

		var frame = new FrameLayout(this);
		createView(item, viewPool, frame);
		setContentView(frame);

		AweryApp.setOnBackPressedListener(this, this::finish);
	}

	private void createView(
			@NonNull SettingsItem item,
			RecyclerView.RecycledViewPool viewPool,
			@NonNull FrameLayout frame
	) {
		item.restoreValues(AwerySettings.getInstance(this));

		var binding = ScreenSettingsBinding.inflate(getLayoutInflater());
		binding.recycler.setRecycledViewPool(viewPool);

		setOnApplyUiInsetsListener(binding.getRoot(), insets ->
				setTopPadding(binding.recycler, insets.top + ViewUtil.dpPx(12)), frame);

		var config = new ConcatAdapter.Config.Builder()
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
				.build();

		if(item.getItems() != null) {
			var recyclerAdapter = new SettingsAdapter(item, this);

			var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
				var headerBinding = LayoutHeaderSettingsBinding.inflate(getLayoutInflater(), parent, false);
				headerBinding.back.setOnClickListener(v -> finish());
				headerBinding.title.setText(item.getTitle(this));

				setOnApplyUiInsetsListener(headerBinding.getRoot(), insets ->
						setHorizontalPadding(headerBinding.getRoot(), insets.left, insets.right), parent);

				return headerBinding;
			});

			binding.recycler.setAdapter(new ConcatAdapter(config, headerAdapter, recyclerAdapter));
		} else if(item.getBehaviour() != null) {
			SettingsData.getScreen(item.getBehaviour(), (screen, e) -> {
				if(e != null) {
					Log.e(TAG, "Failed to get settings", e);
					toast(this, e.getMessage(), 0);
					finish();
					return;
				}

				var recyclerAdapter = new SettingsAdapter(screen, new SettingsAdapter.DataHandler() {

					@Override
					public void onScreenLaunchRequest(SettingsItem item) {
						toast("Currently not supported", 1);
					}

					@Override
					public void save(SettingsItem item, Object newValue) {
						toast("Currently not supported", 1);
					}
				});

				var headerAdapter = SingleViewAdapter.fromBindingDynamic(parent -> {
					var headerBinding = LayoutHeaderSettingsBinding.inflate(getLayoutInflater(), parent, false);
					headerBinding.back.setOnClickListener(v -> finish());
					headerBinding.title.setText(screen.getTitle(this));

					setOnApplyUiInsetsListener(headerBinding.getRoot(), insets ->
							setHorizontalPadding(headerBinding.getRoot(), insets.left, insets.right), parent);

					return headerBinding;
				});

				binding.recycler.setAdapter(new ConcatAdapter(config, headerAdapter, recyclerAdapter));
			});
		} else {
			toast(this, "Failed to get settings", 0);
			finish();
		}

		frame.addView(binding.getRoot());
	}

	@Override
	public void onScreenLaunchRequest(@NonNull SettingsItem item) {
		item.restoreValues();

		var moshi = new Moshi.Builder().add(new SettingsItem.Adapter()).build();
		var jsonAdapter = moshi.adapter(SettingsItem.class);

		var intent = new Intent(this, SettingsActivity.class);
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