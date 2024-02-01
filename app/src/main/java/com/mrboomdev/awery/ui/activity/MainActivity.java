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
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.ScaleAnimation;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.mrboomdev.awery.data.DataPreferences;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.AnimeFragment;
import com.mrboomdev.awery.ui.fragments.HomeFragment;
import com.mrboomdev.awery.ui.fragments.MangaFragment;
import com.mrboomdev.awery.util.ViewUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import ani.awery.MediaPageTransformer;
import ani.awery.R;
import ani.awery.databinding.MainActivityLayoutBinding;
import ani.awery.databinding.SplashScreenBinding;
import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
	private MainActivityLayoutBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		binding = MainActivityLayoutBinding.inflate(getLayoutInflater());
		binding.getRoot().setMotionEventSplittingEnabled(false);
		var prefs = DataPreferences.getInstance(this);
		setContentView(binding.getRoot());

		if(!prefs.getBoolean(DataPreferences.COLOR_OVERFLOW)) {
			binding.navbar.setBackground(ContextCompat.getDrawable(this, R.drawable.bottom_nav_gray));
		} else {
			var backgroundDrawable = (GradientDrawable)binding.navbar.getBackground();
			var colorStateList = backgroundDrawable.getColor();

			var currentColor = colorStateList != null ? colorStateList.getDefaultColor() : 0;
			var semiTransparentColor = (currentColor & 0x00FFFFFF) | 0xE8000000;

			backgroundDrawable.setColor(semiTransparentColor);
			binding.navbar.setBackground(backgroundDrawable);
		}

		startSplash();
		registerBackListener();

		var pagesAdapter = new MainFragmentAdapter(getSupportFragmentManager(), getLifecycle());
		binding.pages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
		binding.pages.setAdapter(pagesAdapter);
		binding.pages.setUserInputEnabled(false);

		binding.pages.setPageTransformer((page, position) -> {
			if(position != 0) return;
			ObjectAnimator.ofFloat(page, "alpha", 0, 1).setDuration(150).start();

			var anim = new ScaleAnimation(
					1.25f, 1f, 1.25f, 1f,
					Animation.RELATIVE_TO_SELF,
					0.5f,
					Animation.RELATIVE_TO_SELF,
					0);

			anim.setDuration(200);
			anim.setInterpolator(this, R.anim.over_shoot);
			page.startAnimation(anim);
		});

		var currentPage = Pages.valueOf(prefs.getString(DataPreferences.DEFAULT_MAIN_PAGE, Pages.HOME.name()));
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

		ViewUtil.setOnApplyUiInsetsListener(binding.bottomSideBarrier, (view, insets) -> ViewUtil.setBottomMargin(view, insets.bottom));
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

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if(doubleBackToExitPressedOnce.get()) {
					finish();
				}

				doubleBackToExitPressedOnce.set(true);
				snackString(getString(R.string.back_to_exit), null, null);

				new Handler(Looper.getMainLooper()).postDelayed(() ->
						doubleBackToExitPressedOnce.set(false), 2000);
			}
		});
	}

	private void startSplash() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			getSplashScreen().setOnExitAnimationListener(
					view -> slideFromSplash(view, view::remove));
		} else {
			var splash = SplashScreenBinding.inflate(getLayoutInflater());
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