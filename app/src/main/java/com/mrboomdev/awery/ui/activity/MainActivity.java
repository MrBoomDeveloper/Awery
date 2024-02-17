package com.mrboomdev.awery.ui.activity;

import static ani.awery.FunctionsKt.snackString;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.AnimeFragment;
import com.mrboomdev.awery.ui.fragments.HomeFragment;
import com.mrboomdev.awery.ui.fragments.MangaFragment;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.FadeTransformer;

import java.util.concurrent.atomic.AtomicBoolean;

import ani.awery.R;
import ani.awery.databinding.LayoutActivityMainBinding;
import ani.awery.databinding.LayoutSplashScreenBinding;
import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
	private LayoutActivityMainBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		binding = LayoutActivityMainBinding.inflate(getLayoutInflater());
		binding.getRoot().setMotionEventSplittingEnabled(false);
		var prefs = AwerySettings.getInstance(this);
		setContentView(binding.getRoot());

		startSplash();
		registerBackListener();

		var pagesAdapter = new MainFragmentAdapter(getSupportFragmentManager(), getLifecycle());
		binding.pages.setAdapter(pagesAdapter);
		binding.pages.setUserInputEnabled(false);
		binding.pages.setPageTransformer(new FadeTransformer());

		var currentPage = Pages.valueOf(prefs.getString(AwerySettings.UI_DEFAULT_MAIN_PAGE, Pages.HOME.name()));
		int currentPageIndex = currentPage.ordinal();
		binding.navbar.selectTabAt(currentPageIndex, false);
		binding.pages.setCurrentItem(currentPageIndex, false);
		binding.navbar.setBackground(ContextCompat.getDrawable(this, R.drawable.bottom_nav_gray));

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

	private void registerBackListener() {
		final var doubleBackToExitPressedOnce = new AtomicBoolean(false);

		AweryApp.setOnBackPressedListener(this, () -> {
			if(doubleBackToExitPressedOnce.get()) {
				finish();
			}

			doubleBackToExitPressedOnce.set(true);
			snackString(getString(R.string.back_to_exit), null, null);

			new Handler(Looper.getMainLooper()).postDelayed(() ->
					doubleBackToExitPressedOnce.set(false), 2000);
		});
	}

	private void startSplash() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			getSplashScreen().setOnExitAnimationListener(
					view -> slideFromSplash(view, view::remove));
		} else {
			var splash = LayoutSplashScreenBinding.inflate(getLayoutInflater());
			binding.getRoot().addView(splash.getRoot());
			((Animatable)splash.splashImage.getDrawable()).start();

			new Handler(Looper.getMainLooper()).postDelayed(() ->
					slideFromSplash(splash.getRoot(), () ->
							binding.getRoot().removeView(splash.getRoot())), 1000);
		}
	}

	private void slideFromSplash(View view, Runnable endCallback) {
		var animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -view.getHeight());
		animator.setInterpolator(new AnticipateInterpolator());
		animator.setDuration(200);

		animator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(@NonNull Animator animation) {}

			@Override
			public void onAnimationEnd(@NonNull Animator animation) {
				endCallback.run();
			}

			@Override
			public void onAnimationCancel(@NonNull Animator animation) {}

			@Override
			public void onAnimationRepeat(@NonNull Animator animation) {}
		});

		animator.start();
	}

	enum Pages {
		ANIME,
		HOME,
		MANGA
	}

	private static class MainFragmentAdapter extends FragmentStateAdapter {


		public MainFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
			super(fragmentManager, lifecycle);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			return switch(position) {
				case 0 -> new AnimeFragment();
				case 1 -> new HomeFragment();
				case 2 -> new MangaFragment();
				default -> throw new RuntimeException("Invalid page position!" + position);
			};
		}

		@Override
		public int getItemCount() {
			return 3;
		}
	}
}