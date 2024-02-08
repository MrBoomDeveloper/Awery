package com.mrboomdev.awery.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.MediaCommentsFragment;
import com.mrboomdev.awery.ui.fragments.MediaInfoFragment;
import com.mrboomdev.awery.ui.fragments.MediaPlayFragment;
import com.mrboomdev.awery.ui.fragments.MediaRelationsFragment;
import com.mrboomdev.awery.util.ui.FadeTransformer;
import com.mrboomdev.awery.util.ui.ViewUtil;

import ani.awery.databinding.MediaDetailsActivityBinding;

public class MediaDetailsActivity extends AppCompatActivity {
	private MediaDetailsActivityBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		binding = MediaDetailsActivityBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), getLifecycle()));
		binding.pager.setUserInputEnabled(false);
		binding.pager.setPageTransformer(new FadeTransformer());

		var navigation = (NavigationBarView) binding.navigation;

		ViewUtil.setOnApplyUiInsetsListener(navigation, (view, insets) -> {
			if(view instanceof NavigationRailView) {
				view.setPadding(insets.left, insets.top, 0, 0);
			} else {
				ViewUtil.setBottomPadding(view, insets.bottom, false);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		var navigation = (NavigationBarView) binding.navigation;

		navigation.setOnItemSelectedListener(item -> {
			binding.pager.setCurrentItem(item.getItemId(), false);
			return true;
		});
	}

	private static class PagerAdapter extends FragmentStateAdapter {

		public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
			super(fragmentManager, lifecycle);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			return switch(position) {
				case 0 -> new MediaInfoFragment();
				case 1 -> new MediaPlayFragment();
				case 2 -> new MediaCommentsFragment();
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