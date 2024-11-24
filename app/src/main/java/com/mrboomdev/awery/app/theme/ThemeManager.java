package com.mrboomdev.awery.app.theme;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.App;
import com.mrboomdev.awery.generated.AwerySettings;

public class ThemeManager {

	public static AwerySettings.ThemeColorPalette_Values getCurrentColorPalette() {
		var current = AwerySettings.THEME_COLOR_PALETTE.getValue();
		return current != null ? current : resetPalette();
	}

	private static AwerySettings.ThemeColorPalette_Values resetPalette() {
		boolean isMaterialYouSupported = DynamicColors.isDynamicColorAvailable();

		var value = isMaterialYouSupported
				? AwerySettings.ThemeColorPalette_Values.MATERIAL_YOU
				: AwerySettings.ThemeColorPalette_Values.RED;

		getPrefs().setValue(AwerySettings.THEME_COLOR_PALETTE, value).saveSync();
		return value;
	}

	public static boolean isDarkModeEnabled() {
		var config = getAnyContext().getResources().getConfiguration();
		return (config.uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
	}

	public static void applyApp(Context context) {
		var isDarkModeEnabled = AwerySettings.USE_DARK_THEME.getValue(null);
		
		// Light theme on tv is an really bad thing.
		if(App.Companion.isTv()) isDarkModeEnabled = true;

		if(isDarkModeEnabled != null) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				context.getSystemService(UiModeManager.class)
						.setApplicationNightMode(isDarkModeEnabled
								? UiModeManager.MODE_NIGHT_YES
								: UiModeManager.MODE_NIGHT_NO);
			} else {
				AppCompatDelegate.setDefaultNightMode(isDarkModeEnabled
						? AppCompatDelegate.MODE_NIGHT_YES
						: AppCompatDelegate.MODE_NIGHT_NO);
			}
		}
	}

	public static void apply(Activity activity, @Nullable Bitmap bitmap) {
		apply(activity, getCurrentColorPalette(), Boolean.TRUE.equals(AwerySettings.USE_AMOLED_THEME.getValue(null)), bitmap);
	}

	public static void apply(
			Activity activity,
			AwerySettings.ThemeColorPalette_Values palette,
			boolean useOLED,
			@Nullable Bitmap bitmap
	) {
		var useColorsFromPoster = Boolean.TRUE.equals(AwerySettings.EXTRACT_BANNER_COLOR.getValue(null));

		if(palette == AwerySettings.ThemeColorPalette_Values.MATERIAL_YOU || (Boolean.TRUE.equals(useColorsFromPoster) && bitmap != null)) {
			applyMaterialYou(activity, bitmap, useOLED);
			return;
		}

		activity.setTheme(getThemeRes(palette, useOLED && isDarkModeEnabled()));
	}

	public static void apply(Activity activity) {
		apply(activity, null);
	}

	private static void applyMaterialYou(Activity activity, Bitmap bitmap, boolean useOLED) {
		var options = new DynamicColorsOptions.Builder();

		if(bitmap != null) {
			options.setContentBasedSource(bitmap);
		}

		if(useOLED) {
			options.setThemeOverlay(R.style.AmoledThemeOverlay);
		}

		DynamicColors.applyToActivityIfAvailable(activity, options.build());
	}

	@SuppressLint("PrivateResource")
	public static int getThemeRes(@NonNull AwerySettings.ThemeColorPalette_Values theme, boolean isAmoled) {
		// Amoled theme breaks some colors in a light theme.
		isAmoled = isAmoled && isDarkModeEnabled();

		return switch(theme) {
			case RED -> isAmoled ? R.style.Theme_Awery_Red_Amoled : R.style.Theme_Awery_Red;
			case PINK -> isAmoled ? R.style.Theme_Awery_Pink_Amoled : R.style.Theme_Awery_Pink;
			case PURPLE -> isAmoled ? R.style.Theme_Awery_Purple_Amoled : R.style.Theme_Awery_Purple;
			case BLUE -> isAmoled ? R.style.Theme_Awery_Blue_Amoled : R.style.Theme_Awery_Blue;
			case GREEN -> isAmoled ? R.style.Theme_Awery_Green_Amoled : R.style.Theme_Awery_Green;
			case MONOCHROME -> isAmoled ? R.style.Theme_Awery_Monochrome_Amoled : R.style.Theme_Awery_Monochrome;
			case MATERIAL_YOU -> com.google.android.material.R.style.Theme_Material3_DynamicColors_DayNight;
		};
	}
}