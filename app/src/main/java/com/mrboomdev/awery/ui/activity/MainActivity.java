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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.mrboomdev.awery.data.DataPreferences;
import com.mrboomdev.awery.ui.ThemeManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ani.awery.R;
import ani.awery.databinding.MainActivityLayoutBinding;
import ani.awery.databinding.SplashScreenBinding;
import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
	private MainActivityLayoutBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		new ThemeManager(this).applyTheme();
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

		startSplash();

		var currentPage = Pages.valueOf(prefs.getString(DataPreferences.DEFAULT_MAIN_PAGE, Pages.HOME.toString()));
		binding.navbar.selectTabAt(currentPage.ordinal(), false);
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
}