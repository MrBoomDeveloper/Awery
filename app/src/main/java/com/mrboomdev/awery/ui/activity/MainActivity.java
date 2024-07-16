package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.addOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.AweryApp.getNavigationStyle;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryApp.removeOnBackPressedListener;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.runDelayed;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setRightPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.CrashHandler;
import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.data.db.item.DBTab;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.databinding.LayoutHeaderHomeBinding;
import com.mrboomdev.awery.databinding.ScreenMainBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.activity.search.MultiSearchActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.ui.fragments.feeds.FeedsFragment;
import com.mrboomdev.awery.util.IconStateful;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TabsTemplate;
import com.mrboomdev.awery.util.ui.EmptyView;
import com.mrboomdev.awery.util.ui.FadeTransformer;

import java.io.File;
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
	private static final String TAG = "MainActivity";
	private static final String SAVED_TAB_INDEX = "was_tab";
	private ScreenMainBinding binding;
	private List<DBTab> tabs;
	private int tabIndex = -1;

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

		if(savedInstanceState != null) {
			tabIndex = savedInstanceState.getInt(SAVED_TAB_INDEX, -1);
		}

		var template = AwerySettings.TABS_TEMPLATE.getValue();
		if(template.equals("custom")) loadCustomTabs(); else loadTemplateTabs(template);
	}

	private void loadCustomTabs() {
		thread(() -> setupTabs(getDatabase().getTabsDao().getAllTabs()));
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
			runOnUiThread(this::setupEmpty);
			return;
		}

		Collections.sort(tabs);

		var savedDefaultTab = AwerySettings.DEFAULT_HOME_TAB.getValue();
		Map<String, IconStateful> icons;

		try {
			var json = readAssets("icons.json");
			var adapter = Parser.<Map<String, IconStateful>>getAdapter(Map.class, String.class, IconStateful.class);
			icons = Parser.fromString(adapter, json);
		} catch(IOException e) {
			Log.e(TAG, "Failed to read an icons atlas!", e);
			CrashHandler.showFatalErrorDialog(this, "Failed to read an icons list!", e);
			return;
		}

		runOnUiThread(() -> {
			setupNavigation();

			switch(getNavigationStyle()) {
				case BUBBLE -> {
					for(int i = 0; i < tabs.size(); i++) {
						var tab = tabs.get(i);
						var icon = icons.get(tab.icon);

						var drawable = icon != null ? icon.getDrawable(this) :
								ContextCompat.getDrawable(this, R.drawable.ic_view_cozy);

						var navbarItem = new AnimatedBottomBar.Tab(
								Objects.requireNonNull(drawable), tab.title,
								i, null, true);

						if(tabIndex == -1 && tab.id.equals(savedDefaultTab)) {
							tabIndex = i;
						}

						binding.navbarBubble.addTab(navbarItem);
					}

					binding.navbarBubble.selectTabAt(tabIndex != -1 ? tabIndex : 0, false);
					binding.navbarBubble.setVisibility(View.VISIBLE);
				}

				case MATERIAL -> {
					var nav = (NavigationBarView) binding.navbarMaterial;
					nav.setBackgroundTintList(ColorStateList.valueOf(SurfaceColors.SURFACE_2.getColor(this)));
					getWindow().setNavigationBarColor(isLandscape() ? 0 : SurfaceColors.SURFACE_2.getColor(this));

					for(int i = 0; i < tabs.size(); i++) {
						var tab = tabs.get(i);
						var icon = icons.get(tab.icon);

						var drawable = icon != null ? icon.getDrawable(this) :
								ContextCompat.getDrawable(this, R.drawable.ic_view_cozy);

						if(tabIndex == -1 && tab.id.equals(savedDefaultTab)) {
							tabIndex = i;
						}

						nav.getMenu().add(0, i, 0, tab.title);
						nav.getMenu().getItem(i).setIcon(drawable);
					}

					nav.setSelectedItemId(tabIndex != -1 ? tabIndex : 0);
					nav.setVisibility(View.VISIBLE);
				}
			}
		});
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
	 * Called in middle of setupTabs()
	 */
	private void setupNavigation() {
		binding = ScreenMainBinding.inflate(getLayoutInflater());
		binding.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));
		setContentView(binding.getRoot());

		var pagesAdapter = new FeedsAdapter(tabs, getSupportFragmentManager(), getLifecycle());
		binding.pages.setAdapter(pagesAdapter);
		binding.pages.setUserInputEnabled(false);
		binding.pages.setPageTransformer(new FadeTransformer());

		if(AwerySettings.USE_AMOLED_THEME.getValue()) {
			binding.navbarMaterial.setBackgroundColor(0x00000000);
		}

		setOnApplyUiInsetsListener(binding.navbarMaterial, insets -> {
			setTopPadding(binding.navbarMaterial, (binding.navbarMaterial instanceof NavigationRailView) ? insets.top : 0);

			setLeftPadding(binding.navbarMaterial, insets.left);
			setBottomPadding(binding.navbarMaterial, insets.bottom);
			return true;
		});

		setOnApplyUiInsetsListener(binding.bottomSideBarrier, insets -> {
			setBottomMargin(binding.bottomSideBarrier, insets.bottom);
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
			outState.putInt(SAVED_TAB_INDEX, binding.pages.getCurrentItem());
		}

		super.onSaveInstanceState(outState);
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
			var tab = tabs.get(position);
			var arguments = new Bundle();

			arguments.putSerializable(FeedsFragment.ARGUMENT_TAB, tab);
			arguments.putSerializable(FeedsFragment.ARGUMENT_FEEDS, (Serializable) tab.feeds);

			var fragment = new HomeFeedsFragment();
			fragment.setArguments(arguments);

			fragments.set(position, new WeakReference<>(fragment));
			return fragment;
		}

		@Override
		public int getItemCount() {
			return tabs.size();
		}
	}

	public static class HomeFeedsFragment extends FeedsFragment {
		private LayoutHeaderHomeBinding header;
		private boolean isHeaderTransparent;

		@Override
		protected View getHeader(ViewGroup parent) {
			if(header != null) {
				return header.getRoot();
			}

			var binding = LayoutHeaderHomeBinding.inflate(
					LayoutInflater.from(parent.getContext()), parent, false);

			// Note: We do this because the string in resources doesn't tell whatever the app is beta or stable
			binding.title.setText(requireContext().getApplicationInfo().loadLabel(requireContext().getPackageManager()));

			// TODO: Make visible once notifications activity will be done
			binding.notifications.setVisibility(View.GONE);

			binding.search.setOnClickListener(v -> {
				var intent = new Intent(requireActivity(), MultiSearchActivity.class);
				startActivity(intent);
			});

			if(binding.searchBar != null) {
				binding.searchBar.setOnClickListener(v -> binding.search.performClick());
			}

			binding.settingsWrapper.setOnClickListener(v -> {
				var intent = new Intent(requireActivity(), SettingsActivity.class);
				startActivity(intent);
			});

			setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
				setTopPadding(binding.getRoot(), insets.top + dpPx(binding, 16));

				if(isLandscape()) {
					setLeftPadding(binding.getRoot(), dpPx(binding, 32) +
							(getNavigationStyle() == AwerySettings.NavigationStyle_Values.MATERIAL ? 0 : insets.left));

					setRightPadding(binding.getRoot(), dpPx(binding, 32) + insets.right);
				} else {
					setHorizontalPadding(binding.getRoot(), dpPx(binding, 16));
				}

				return false;
			});

			header = binding;

			if(isHeaderTransparent) {
				updateHeader(false);
			}

			return binding.getRoot();
		}

		@Override
		public void setContentBehindToolbarEnabled(boolean isEnabled) {
			super.setContentBehindToolbarEnabled(isEnabled);
			isHeaderTransparent = isEnabled;

			if(header != null) {
				updateHeader(isEnabled);
			}
		}

		private void updateHeader(boolean isTransparent) {
			if(isTransparent) {
				if(header.searchBar != null) {
					header.logo.setVisibility(View.GONE);
					header.searchBar.setVisibility(View.GONE);
				}

				header.title.setVisibility(View.GONE);
				header.search.setVisibility(View.VISIBLE);
			} else {
				header.title.setVisibility(View.VISIBLE);
				header.logo.setVisibility(View.VISIBLE);

				if(header.searchBar != null) {
					header.search.setVisibility(View.GONE);
					header.searchBar.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		protected SettingsList getFilters() {
			return new SettingsList();
		}

		@Override
		protected boolean loadOnStartup() {
			return true;
		}

		@Nullable
		@Override
		protected File getCacheFile() {
			return new File(requireContext().getCacheDir(), Constants.DIRECTORY_NET_CACHE + "/" + Constants.FILE_FEEDS_NET_CACHE);
		}
	}
}