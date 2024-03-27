package com.mrboomdev.awery.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.MediaDetailsActivityBinding;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.MediaCommentsFragment;
import com.mrboomdev.awery.ui.fragments.MediaInfoFragment;
import com.mrboomdev.awery.ui.fragments.MediaPlayFragment;
import com.mrboomdev.awery.ui.fragments.MediaRelationsFragment;
import com.mrboomdev.awery.util.ui.FadeTransformer;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.io.IOException;
import java.util.Objects;

public class MediaActivity extends AppCompatActivity {
	private static final String TAG = "MediaActivity";
	private MediaDetailsActivityBinding binding;
	private CatalogMedia media;

	@SuppressLint("NonConstantResourceId")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		binding = MediaDetailsActivityBinding.inflate(getLayoutInflater());
		binding.pager.setUserInputEnabled(false);
		binding.pager.setPageTransformer(new FadeTransformer());

		var navigation = (NavigationBarView) binding.navigation;

		ViewUtil.setOnApplyUiInsetsListener(navigation, insets -> {
			if(navigation instanceof NavigationRailView) {
				navigation.setPadding(insets.left, insets.top, 0, 0);
			} else {
				ViewUtil.setBottomPadding(navigation, insets.bottom, false);
			}
		});

		if(binding.navigation instanceof NavigationRailView rail) {
			var style = com.google.android.material.R.attr.floatingActionButtonSmallSecondaryStyle;
			var header = new FloatingActionButton(this, null, style);
			header.setImageResource(R.drawable.ic_round_arrow_back_ios_new_24);
			header.setOnClickListener(v -> finish());
			rail.addHeaderView(header);
		}

		try {
			var adapter = CatalogMedia.getJsonAdapter();
			var json = getIntent().getStringExtra("media");

			var media = adapter.fromJson(Objects.requireNonNull(json));
			if(media == null) throw new NullPointerException("Media is null!");

			new Thread(() -> {
				var db = AweryApp.getDatabase().getMediaDao();
				var dbMedia = db.get(media.globalId);

				if(dbMedia != null) {
					media.merge(dbMedia.toCatalogMedia());
				}

				runOnUiThread(() -> setMedia(media));
			}).start();
		} catch(IOException e) {
			AweryApp.toast(this, "Failed to load media!", 1);
			Log.e(TAG, "Failed to load media!", e);
			finish();
		}
	}

	@SuppressLint("NonConstantResourceId")
	public void setMedia(CatalogMedia media) {
		this.media = media;
		binding.pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), getLifecycle()));

		var navigation = (NavigationBarView) binding.navigation;

		navigation.setOnItemSelectedListener(item -> {
			binding.pager.setCurrentItem(switch(item.getItemId()) {
				case R.id.info -> 0;
				case R.id.watch -> 1;
				case R.id.comments -> 2;
				case R.id.relations -> 3;
				default -> throw new IllegalArgumentException("Invalid item id: " + item.getItemId());
			}, false);
			return true;
		});

		launchAction(Objects.requireNonNull(getIntent().getStringExtra("action")));
		setContentView(binding.getRoot());
	}

	public void launchAction(@NonNull String action) {
		var navigation = (NavigationBarView) binding.navigation;

		navigation.setSelectedItemId(switch(action) {
			case "info" -> R.id.info;
			case "watch" -> R.id.watch;
			case "comments" -> R.id.comments;
			case "relations" -> R.id.relations;
			default -> throw new IllegalArgumentException("Invalid action: " + getIntent().getStringExtra("action"));
		});
	}

	private class PagerAdapter extends FragmentStateAdapter {

		public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
			super(fragmentManager, lifecycle);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			return switch(position) {
				case 0 -> new MediaInfoFragment(media);
				case 1 -> new MediaPlayFragment(media);
				case 2 -> new MediaCommentsFragment(media);
				case 3 -> new MediaRelationsFragment();
				default -> throw new IllegalArgumentException("Invalid position: " + position);
			};
		}

		@Override
		public int getItemCount() {
			return 4;
		}
	}
}