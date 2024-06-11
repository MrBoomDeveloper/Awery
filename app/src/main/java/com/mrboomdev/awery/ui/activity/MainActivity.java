package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.addOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.getResourceId;
import static com.mrboomdev.awery.app.AweryApp.removeOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.snackbar;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.util.NiceUtils.findIn;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.data.settings.NicePreferences;
import com.mrboomdev.awery.databinding.ScreenMainBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.AnimeFragment;
import com.mrboomdev.awery.ui.fragments.LibraryFragment;
import com.mrboomdev.awery.ui.fragments.MangaFragment;
import com.mrboomdev.awery.util.IconStateful;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.ui.FadeTransformer;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
	public ScreenMainBinding binding;
	private static final String TAG = "MainActivity";
	private static final int CUSTOM_TABS_START = -99999;
	private List<DBTab> tabs;

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

		var prefs = NicePreferences.getPrefs();
		var savedDefaultTab = prefs.getString(AwerySettings.DEFAULT_HOME_TAB);
		var currentPage = StringUtils.parseEnum(savedDefaultTab, Pages.MAIN);
		int currentPageIndex = currentPage.ordinal();

		binding.navbar.selectTabAt(currentPageIndex, false);
		binding.pages.setCurrentItem(currentPageIndex, false);

		binding.navbar.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {

			@Override
			public void onTabSelected(
					int previousIndex,
					@Nullable AnimatedBottomBar.Tab previousTab,
					int currentIndex,
					@NonNull AnimatedBottomBar.Tab currentTab
			) {
				binding.pages.setCurrentItem(currentIndex, false);
			}

			@Override
			public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {}
		});

		ViewUtil.setOnApplyUiInsetsListener(binding.bottomSideBarrier, insets -> {
			ViewUtil.setBottomMargin(binding.bottomSideBarrier, insets.bottom);
			return true;
		});

		// TODO: Manga screen
		binding.navbar.removeTabAt(2);

		new Thread(() -> {
			tabs = getDatabase().getTabsDao().getAllTabs();

			if(tabs.isEmpty()) {
				//var json = readAssets("templates.json");
				return;
			}

			for(var tab : binding.navbar.getTabs()) {
				binding.navbar.removeTab(tab);
			}

			Collections.sort(tabs);
			Map<String, IconStateful> icons = null;

			try {
				var json = readAssets("icons.json");
				var adapter = Parser.<Map<String, IconStateful>>getAdapter(Map.class, String.class, IconStateful.class);
				icons = Parser.fromString(adapter, json);
			} catch(IOException e) {
				Log.e(TAG, "Failed to read an icons atlas!", e);
				CrashHandler.showErrorDialog(this, "Failed to read an icons list!", e);
			}

			AnimatedBottomBar.Tab selectedNavbarItem = null;

			for(int i = 0; i < tabs.size(); i++) {
				Drawable drawable = null;
				var tab = tabs.get(i);

				if(icons != null) {
					var icon = icons.get(tab.icon);

					if(icon != null) {
						var id = getResourceId(R.drawable.class, icon.getInActive());
						drawable = ContextCompat.getDrawable(this, id);
					}
				}

				if(drawable == null) {
					drawable = ContextCompat.getDrawable(
							this, R.drawable.ic_view_cozy);
				}

				var navbarItem = new AnimatedBottomBar.Tab(
						Objects.requireNonNull(drawable),
						tab.title,
						CUSTOM_TABS_START + i,
						null,
						true);

				if(tab.id.equals(savedDefaultTab)) {
					selectedNavbarItem = navbarItem;
				}

				binding.navbar.addTabAt(i, navbarItem);
			}

			if(selectedNavbarItem == null) {
				selectedNavbarItem = binding.navbar.getTabs().get(0);
			}

			binding.navbar.selectTab(selectedNavbarItem, false);
		}).start();
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
				default -> throw new IllegalArgumentException("Invalid page position! " + position);
			};
		}

		@Override
		public int getItemCount() {
			return 3;
		}
	}
}