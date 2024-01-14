package com.mrboomdev.awery.ui.activity;

import static ani.awery.FunctionsKt.snackString;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.data.DataPreferences;
import com.mrboomdev.awery.ui.ThemeManager;

import java.util.concurrent.atomic.AtomicBoolean;

import ani.awery.R;
import ani.awery.databinding.MainActivityLayoutBinding;
import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {
	private MainActivityLayoutBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		new ThemeManager(this).applyTheme();
		super.onCreate(savedInstanceState);

		binding = MainActivityLayoutBinding.inflate(getLayoutInflater());
		var prefs = DataPreferences.getInstance(this);
		setContentView(binding.getRoot());

		AnimatedBottomBar bottomBar = findViewById(R.id.navbar);

		if(!prefs.getBoolean(DataPreferences.COLOR_OVERFLOW)) {
			bottomBar.setBackground(ContextCompat.getDrawable(this, R.drawable.bottom_nav_gray));
		} else {
			var backgroundDrawable = (GradientDrawable)bottomBar.getBackground();
			var colorStateList = backgroundDrawable.getColor();

			var currentColor = colorStateList != null ? colorStateList.getDefaultColor() : 0;
			var semiTransparentColor = (currentColor & 0x00FFFFFF) | 0xE8000000;

			backgroundDrawable.setColor(semiTransparentColor);
			bottomBar.setBackground(backgroundDrawable);
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

		binding.getRoot().setMotionEventSplittingEnabled(false);
	}
}