package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.addOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.getNavigationStyle;
import static com.mrboomdev.awery.app.AweryApp.getResourceId;
import static com.mrboomdev.awery.app.AweryApp.removeOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.databinding.ScreenMainBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.fragments.FeedsFragment;
import com.mrboomdev.awery.util.IconStateful;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TabsTemplate;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.FadeTransformer;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
	public ScreenMainBinding binding;
	private static final String TAG = "MainActivity";
	private List<DBTab> tabs;

	private final Runnable backListener = new Runnable() {
		private boolean doubleBackToExitPressedOnce;

		@Override
		public void run() {
			if(doubleBackToExitPressedOnce) {
				finish();
			}

			doubleBackToExitPressedOnce = true;
			toast(R.string.back_to_exit);
			runDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var template = AwerySettings.TABS_TEMPLATE.getValue();
		if(template.equals("custom")) loadCustomTabs(); else loadTemplateTabs(template);
	}

	private void loadCustomTabs() {
		new Thread(() -> setupTabs(getDatabase().getTabsDao().getAllTabs())).start();
	}

	private void loadTemplateTabs(String templateName) {
		try {
			var templateJson = readAssets("tabs_templates.json");
			var adapter = Parser.<List<TabsTemplate>>getAdapter(List.class, TabsTemplate.class);
			var templates = Parser.fromString(adapter, templateJson);

			var selected = find(templates, template -> template.id.equals(templateName));
			setupTabs(selected != null ? selected.tabs : Collections.emptyList());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupTabs(@NonNull List<DBTab> tabs) {
		this.tabs = tabs;

		if(tabs.isEmpty()) {
			setupEmpty();
			return;
		}

		setupNavigation();
		Collections.sort(tabs);

		var savedDefaultTab = AwerySettings.DEFAULT_HOME_TAB.getValue();
		Map<String, IconStateful> icons = null;

		try {
			var json = readAssets("icons.json");
			var adapter = Parser.<Map<String, IconStateful>>getAdapter(Map.class, String.class, IconStateful.class);
			icons = Parser.fromString(adapter, json);
		} catch(IOException e) {
			Log.e(TAG, "Failed to read an icons atlas!", e);
			CrashHandler.showErrorDialog(this, "Failed to read an icons list!", e);
		}

		int selected = 0;

		switch(getNavigationStyle()) {
			case BUBBLE -> {
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
							Objects.requireNonNull(drawable), tab.title,
							i, null, true);

					if(tab.id.equals(savedDefaultTab)) {
						selected = i;
					}

					binding.navbarBubble.addTab(navbarItem);
				}

				binding.navbarBubble.selectTabAt(selected, false);
				binding.navbarBubble.setVisibility(View.VISIBLE);
			}

			case MATERIAL -> {
				var nav = (NavigationBarView) binding.navbarMaterial;

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

					if(tab.id.equals(savedDefaultTab)) {
						selected = i;
					}

					nav.getMenu().add(0, i, 0, tab.title);
					nav.getMenu().getItem(i).setIcon(drawable);
				}

				nav.setSelectedItemId(selected);
				nav.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Called after setupTabs() if no tabs was found
	 */
	private void setupEmpty() {
		var binding = new EmptyView(this);

		binding.setInfo("No tabs found",
				"Please selecting an template or either create your own tabs to see anything here.",
				"Go to settings", () -> startActivity(new Intent(this, SettingsActivity.class)));

		setContentView(binding.getRoot());
	}

	/**
	 * Called after setupTabs()
	 */
	private void setupNavigation() {
		binding = ScreenMainBinding.inflate(getLayoutInflater());
		binding.getRoot().setMotionEventSplittingEnabled(false);
		setContentView(binding.getRoot());

		var pagesAdapter = new FeedsAdapter(tabs, getSupportFragmentManager(), getLifecycle());
		binding.pages.setAdapter(pagesAdapter);
		binding.pages.setUserInputEnabled(false);
		binding.pages.setPageTransformer(new FadeTransformer());

		setOnApplyUiInsetsListener(binding.navbarMaterial, insets -> {
			setTopPadding(binding.navbarMaterial, (binding.navbarMaterial instanceof NavigationRailView) ? insets.top : 0);

			setLeftPadding(binding.navbarMaterial, insets.left);
			setBottomPadding(binding.navbarMaterial, insets.bottom);
			return true;
		});

		setOnApplyUiInsetsListener(binding.bottomSideBarrier, insets -> {
			ViewUtil.setBottomMargin(binding.bottomSideBarrier, insets.bottom);
			return true;
		});

		((NavigationBarView) binding.navbarMaterial).setOnItemSelectedListener(item -> {
			binding.pages.setCurrentItem(item.getItemId(), false);
			return true;
		});

		((NavigationBarView) binding.navbarMaterial).setOnItemReselectedListener(item -> {
			var adapter = (FeedsAdapter) binding.pages.getAdapter();
			if(adapter == null) return;

			var ref = adapter.fragments.get(item.getItemId());
			if(ref == null) return;

			var fragment = ref.get();
			if(fragment == null) return;

			fragment.scrollToTop();
		});

		binding.navbarBubble.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {

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
			public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab tab) {
				var adapter = (FeedsAdapter) binding.pages.getAdapter();
				if(adapter == null) return;

				var ref = adapter.fragments.get(i);
				if(ref == null) return;

				var fragment = ref.get();
				if(fragment == null) return;

				fragment.scrollToTop();
			}
		});
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
		if(binding != null) {
			outState.putInt("nav_index", switch(getNavigationStyle()) {
				case BUBBLE -> binding.navbarBubble.getSelectedIndex();
				case MATERIAL -> ((NavigationBarView) binding.navbarMaterial).getSelectedItemId();
			});
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		if(binding != null) {
			/*var index = savedInstanceState.getInt("nav_index");
			binding.navbarBubble.selectTabAt(index, false);
			((NavigationBarView) binding.navbarMaterial).setSelectedItemId(index);*/
		}

		super.onRestoreInstanceState(savedInstanceState);
	}

	private static class FeedsAdapter extends FragmentStateAdapter {
		public final List<WeakReference<FeedsFragment>> fragments;
		private final List<DBTab> tabs;

		public FeedsAdapter(
				@NonNull List<DBTab> tabs,
				@NonNull FragmentManager fragmentManager,
				@NonNull Lifecycle lifecycle
		) {
			super(fragmentManager, lifecycle);
			this.tabs = tabs;

			this.fragments = new ArrayList<>(stream(tabs)
					.map(a -> (WeakReference<FeedsFragment>) null)
					.toList());
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			var arguments = new Bundle();
			arguments.putSerializable("feeds", (Serializable) tabs.get(position).feeds);

			var fragment = new FeedsFragment();
			fragment.setArguments(arguments);

			fragments.set(position, new WeakReference<>(fragment));
			return fragment;
		}

		@Override
		public int getItemCount() {
			return tabs.size();
		}
	}
}