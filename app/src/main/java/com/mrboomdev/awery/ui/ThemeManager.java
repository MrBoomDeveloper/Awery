package com.mrboomdev.awery.ui;

import android.app.Activity;
import android.graphics.Bitmap;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;

import ani.awery.R;

public class ThemeManager {

	public static void apply(Activity activity, Bitmap bitmap) {
		var prefs = AwerySettings.getInstance(activity);

		boolean useOLED = prefs.getBoolean(AwerySettings.THEME_USE_OLDED);
		boolean useMaterialYou = prefs.getBoolean(AwerySettings.THEME_USE_MATERIAL_YOU, DynamicColors.isDynamicColorAvailable());
		boolean useColorsFromPoster = prefs.getBoolean(AwerySettings.THEME_USE_COLORS_FROM_MEDIA);

		if(useMaterialYou || (useColorsFromPoster && bitmap != null)) {
			applyMaterialYou(activity, bitmap, useOLED);
			return;
		}

		var savedTheme = prefs.getString(AwerySettings.THEME_PALLET, Theme.PURPLE.name());
		Theme enumTheme;

		if(savedTheme.equals(AwerySettings.THEME_CUSTOM)) {
			throw new UnimplementedException("Custom theme is not implemented yet");
		}

		try {
			enumTheme = Theme.valueOf(savedTheme);
		} catch(IllegalArgumentException e) {
			enumTheme = Theme.PURPLE;
		}

		var themeToApply = switch(enumTheme) {
			case BLUE -> useOLED ? R.style.Theme_Awery_BlueOLED : R.style.Theme_Awery_Blue;
			case GREEN -> useOLED ? R.style.Theme_Awery_GreenOLED : R.style.Theme_Awery_Green;
			case PINK -> useOLED ? R.style.Theme_Awery_PinkOLED : R.style.Theme_Awery_Pink;
			case RED -> useOLED ? R.style.Theme_Awery_RedOLED : R.style.Theme_Awery_Red;
			case LAVENDER -> useOLED ? R.style.Theme_Awery_LavenderOLED : R.style.Theme_Awery_Lavender;
			case MONOCHROME -> useOLED ? R.style.Theme_Awery_MonochromeOLED : R.style.Theme_Awery_Monochrome;
			case SAIKOU -> useOLED ? R.style.Theme_Awery_SaikouOLED : R.style.Theme_Awery_Saikou;
			case PURPLE -> useOLED ? R.style.Theme_Awery_PurpleOLED : R.style.Theme_Awery_Purple;
		};

		activity.setTheme(themeToApply);
	}

	public static void apply(Activity activity) {
		apply(activity, null);
	}

	private static void applyMaterialYou(Activity activity, Bitmap bitmap, boolean useOLED) {
		var options = new DynamicColorsOptions.Builder();

		if(bitmap != null) options.setContentBasedSource(bitmap);
		if(useOLED) options.setThemeOverlay(R.style.AmoledThemeOverlay);

		DynamicColors.applyToActivityIfAvailable(activity, options.build());
	}

	public enum Theme {
		PURPLE, BLUE, GREEN, PINK, RED, LAVENDER, MONOCHROME, SAIKOU
	}
}