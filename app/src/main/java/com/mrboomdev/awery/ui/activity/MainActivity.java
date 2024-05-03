package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.addOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.removeOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.snackbar;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.app.CrashHandler.reportIfCrashHappened;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.databinding.ScreenMainBinding;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.AnimeFragment;
import com.mrboomdev.awery.ui.fragments.LibraryFragment;
import com.mrboomdev.awery.ui.fragments.MangaFragment;
import com.mrboomdev.awery.util.ui.FadeTransformer;
import com.mrboomdev.awery.util.ui.ViewUtil;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
	private final Runnable backListener = new Runnable() {
		private boolean doubleBackToExitPressedOnce;

		@Override
		public void run() {
			if(doubleBackToExitPressedOnce) {
				finish();
			}

			doubleBackToExitPressedOnce = true;
			snackbar(MainActivity.this, getString(R.string.back_to_exit));
			runDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
		}
	};
	public ScreenMainBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		binding = ScreenMainBinding.inflate(getLayoutInflater());
		binding.getRoot().setMotionEventSplittingEnabled(false);
		setContentView(binding.getRoot());

		var pagesAdapter = new MainFragmentAdapter(getSupportFragmentManager(), getLifecycle());
		binding.pages.setAdapter(pagesAdapter);
		binding.pages.setUserInputEnabled(false);
		binding.pages.setPageTransformer(new FadeTransformer());

		var prefs = AwerySettings.getInstance(this);
		var savedDefaultTab = prefs.getString(AwerySettings.ui.DEFAULT_HOME_TAB);
		var currentPage = StringUtils.parseEnum(savedDefaultTab, Pages.MAIN);
		int currentPageIndex = currentPage.ordinal();

		binding.navbar.selectTabAt(currentPageIndex, false);
		binding.pages.setCurrentItem(currentPageIndex, false);

		binding.navbar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {

			@Override
			public void onTabSelected(int was, @Nullable AnimatedBottomBar.Tab tab, int next, @NonNull AnimatedBottomBar.Tab tab1) {
				binding.pages.setCurrentItem(next, false);
			}

			@Override
			public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {}
		});

		ViewUtil.setOnApplyUiInsetsListener(binding.bottomSideBarrier, insets ->
				ViewUtil.setBottomMargin(binding.bottomSideBarrier, insets.bottom));

		binding.navbar.removeTabAt(2);
		reportIfCrashHappened(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		addOnBackPressedListener(this, backListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		removeOnBackPressedListener(this, backListener);
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putInt("nav_index", binding.navbar.getSelectedIndex());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		binding.navbar.selectTabAt(savedInstanceState.getInt("nav_index"), false);
		super.onRestoreInstanceState(savedInstanceState);
	}

	enum Pages { MAIN, LIBRARY }

	private class MainFragmentAdapter extends FragmentStateAdapter {


		public MainFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
			super(fragmentManager, lifecycle);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			return switch(position) {
				case 0 -> new AnimeFragment().setupWithMainActivity(MainActivity.this, 0);
				case 1 -> new LibraryFragment().setupWithMainActivity(MainActivity.this, 1);
				case 2 -> new MangaFragment().setupWithMainActivity(MainActivity.this, 2);
				default -> throw new IllegalArgumentException("Invalid page position!" + position);
			};
		}

		@Override
		public int getItemCount() {
			return 3;
		}
	}
}