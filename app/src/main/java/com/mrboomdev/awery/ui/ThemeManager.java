package com.mrboomdev.awery.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.StyleRes;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.settings.AwerySettings;

public class ThemeManager {
	private static final Theme DEFAULT_THEME = Theme.PINK;

	public static boolean isAmoled(Context context) {
		var prefs = AwerySettings.getInstance(context);
		return prefs.getBoolean(AwerySettings.THEME_USE_OLDED);
	}

	public static boolean isMaterialYou(Context context) {
		var prefs = AwerySettings.getInstance(context);

		if(!prefs.contains(AwerySettings.THEME_USE_MATERIAL_YOU)) {
			boolean isMaterialYouSupported = DynamicColors.isDynamicColorAvailable();

			prefs.setBoolean(AwerySettings.THEME_USE_MATERIAL_YOU, isMaterialYouSupported);
			prefs.saveAsync();

			return isMaterialYouSupported;
		}

		return prefs.getBoolean(AwerySettings.THEME_USE_MATERIAL_YOU);
	}

	public static void apply(Activity activity, Bitmap bitmap) {
		var prefs = AwerySettings.getInstance(activity);

		boolean useOLED = prefs.getBoolean(AwerySettings.THEME_USE_OLDED);
		boolean useColorsFromPoster = prefs.getBoolean(AwerySettings.THEME_USE_COLORS_FROM_MEDIA);
		boolean useMaterialYou = isMaterialYou(activity);

		if(useMaterialYou || (useColorsFromPoster && bitmap != null)) {
			applyMaterialYou(activity, bitmap, useOLED);
			return;
		}

		var savedTheme = prefs.getString(AwerySettings.THEME_PALLET, DEFAULT_THEME.name());
		Theme enumTheme;

		try {
			enumTheme = Theme.valueOf(savedTheme);
		} catch(IllegalArgumentException e) {
			enumTheme = DEFAULT_THEME;
			prefs.setString(AwerySettings.THEME_PALLET, enumTheme.name());
		}

		activity.setTheme(useOLED ?
				enumTheme.getOledRes() :
				enumTheme.getRes());
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
		PURPLE(R.style.Theme_Awery_Purple, R.style.Theme_Awery_PinkOLED),
		BLUE(R.style.Theme_Awery_Blue, R.style.Theme_Awery_BlueOLED),
		GREEN(R.style.Theme_Awery_Green, R.style.Theme_Awery_GreenOLED),
		PINK(R.style.Theme_Awery_Pink, R.style.Theme_Awery_PinkOLED),
		RED(R.style.Theme_Awery_Red, R.style.Theme_Awery_RedOLED),
		LAVENDER(R.style.Theme_Awery_Lavender, R.style.Theme_Awery_LavenderOLED),
		MONOCHROME(R.style.Theme_Awery_Monochrome, R.style.Theme_Awery_MonochromeOLED),
		SAIKOU(R.style.Theme_Awery_Saikou, R.style.Theme_Awery_SaikouOLED);

		private final int res, oledRes;

		Theme(int res, int oledRes) {
			this.res = res;
			this.oledRes = oledRes;
		}

		@StyleRes
		public int getOledRes() {
			return oledRes;
		}

		@StyleRes
		public int getRes() {
			return res;
		}
	}
}