package com.mrboomdev.awery.ui;

import static android.content.Context.UI_MODE_SERVICE;

import static com.mrboomdev.awery.data.settings.NicePreferences.getPrefs;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.generated.AwerySettings;

import java.util.Objects;

public class ThemeManager {

	public static boolean isMaterialYou() {
		if(!AwerySettings.USE_MATERIAL_YOU.exists()) {
			boolean isMaterialYouSupported = DynamicColors.isDynamicColorAvailable();

			getPrefs().setValue(AwerySettings.USE_MATERIAL_YOU, isMaterialYouSupported).saveAsync();
			return isMaterialYouSupported;
		}

		return AwerySettings.USE_MATERIAL_YOU.getValue();
	}

	public static void applyApp(Context context) {
		var isDarkModeEnabled = AwerySettings.USE_DARK_THEME.getValue(null);

		if(isDarkModeEnabled != null) {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				((UiModeManager) context.getSystemService(UI_MODE_SERVICE))
						.setApplicationNightMode(isDarkModeEnabled
								? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO);
			} else {
				AppCompatDelegate.setDefaultNightMode(isDarkModeEnabled
						? AppCompatDelegate.MODE_NIGHT_YES
						: AppCompatDelegate.MODE_NIGHT_NO);
			}
		}
	}

	public static void apply(Activity activity, Bitmap bitmap) {
		boolean useOLED = AwerySettings.USE_AMOLED_THEME.getValue();
		boolean useColorsFromPoster = AwerySettings.EXTRACT_BANNER_COLOR.getValue();
		boolean useMaterialYou = isMaterialYou();

		if(useMaterialYou || (useColorsFromPoster && bitmap != null)) {
			applyMaterialYou(activity, bitmap, useOLED);
			return;
		}

		var savedTheme = Objects.requireNonNullElse(
				AwerySettings.THEME_COLOR_PALETTE.getValue(),
				AwerySettings.ThemeColorPalette_Values.RED);

		// In light mode there is no amoled theme so we need to check the current night mode
		if(useOLED && (activity.getResources().getConfiguration().uiMode
				& Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
			activity.setTheme(getThemeRes(savedTheme, false));
			return;
		}

		activity.setTheme(getThemeRes(savedTheme, useOLED));
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

	private static int getThemeRes(@NonNull AwerySettings.ThemeColorPalette_Values theme, boolean isAmoled) {
		return switch(theme) {
			case RED -> isAmoled ? R.style.Theme_Awery_RedOLED : R.style.Theme_Awery_Red;
			case PINK -> isAmoled ? R.style.Theme_Awery_PinkOLED : R.style.Theme_Awery_Pink;
			case PURPLE -> isAmoled ? R.style.Theme_Awery_PurpleOLED : R.style.Theme_Awery_Purple;
			case BLUE -> isAmoled ? R.style.Theme_Awery_BlueOLED : R.style.Theme_Awery_Blue;
			case GREEN -> isAmoled ? R.style.Theme_Awery_GreenOLED : R.style.Theme_Awery_Green;
			case MONOCHROME -> isAmoled ? R.style.Theme_Awery_MonochromeOLED : R.style.Theme_Awery_Monochrome;
		};
	}
}