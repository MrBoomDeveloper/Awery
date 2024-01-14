package com.mrboomdev.awery.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.mrboomdev.awery.data.DataPreferences;

import java.util.Objects;

import ani.awery.R;

public class ThemeManager {
	private final Context context;

	public ThemeManager(Context context) {
		this.context = context;
	}

	public void applyTheme(Bitmap fromImage) {
		var prefs = DataPreferences.getInstance(context);

		boolean useOLED = prefs.getBoolean(DataPreferences.USE_OLDED) && isDarkThemeActive(context);
		boolean useCustomTheme = prefs.getBoolean(DataPreferences.USE_CUSTOM_THEME);
		int customTheme = prefs.getInt(DataPreferences.CUSTOM_THEME_INT, 16712221);
		boolean useSource = prefs.getBoolean(DataPreferences.USE_SOURCE_THEME);
		boolean useMaterial = prefs.getBoolean(DataPreferences.USE_MATERIAL_YOU);

		if(useSource) {
			var returnedEarly = applyDynamicColors(
					useMaterial,
					context,
					useOLED,
					fromImage,
					useCustomTheme ? customTheme : 0);

			if(!returnedEarly) return;
		} else if (useCustomTheme) {
			var returnedEarly = applyDynamicColors(useMaterial, context, useOLED, null, customTheme);
			if(!returnedEarly) return;
		} else {
			var returnedEarly = applyDynamicColors(useMaterial, context, useOLED, null, 0);
			if(!returnedEarly) return;
		}

		var theme = context.getSharedPreferences("Awery", Context.MODE_PRIVATE)
				.getString("theme", "PURPLE");

		var themeToApply = switch(theme) {
			case "BLUE" -> (useOLED) ? R.style.Theme_Dantotsu_BlueOLED : R.style.Theme_Dantotsu_Blue;
			case "GREEN" -> (useOLED) ? R.style.Theme_Dantotsu_GreenOLED : R.style.Theme_Dantotsu_Green;
			case "PINK" -> (useOLED) ? R.style.Theme_Dantotsu_PinkOLED : R.style.Theme_Dantotsu_Pink;
			case "RED" -> (useOLED) ? R.style.Theme_Dantotsu_RedOLED : R.style.Theme_Dantotsu_Red;
			case "LAVENDER" -> (useOLED) ? R.style.Theme_Dantotsu_LavenderOLED : R.style.Theme_Dantotsu_Lavender;
			case "MONOCHROME (BETA)" -> (useOLED) ? R.style.Theme_Dantotsu_MonochromeOLED : R.style.Theme_Dantotsu_Monochrome;
			case "SAIKOU" -> (useOLED) ? R.style.Theme_Dantotsu_SaikouOLED : R.style.Theme_Dantotsu_Saikou;
            default -> (useOLED) ? R.style.Theme_Dantotsu_PurpleOLED : R.style.Theme_Dantotsu_Purple;
		};

		context.setTheme(themeToApply);
	}

	public void applyTheme() {
		applyTheme(null);
	}

	private boolean applyDynamicColors(
			boolean useMaterialYou,
			Context context,
			boolean useOLED,
			Bitmap bitmap,
			int useCustom
	) {
		var builder = new DynamicColorsOptions.Builder();
		var needMaterial = true;

		// Set content-based source if a bitmap is provided
		if (bitmap != null) {
			builder.setContentBasedSource(bitmap);
			needMaterial = false;
		} else if(useCustom != 0) {
			builder.setContentBasedSource(useCustom);
			needMaterial = false;
		}

		if(useOLED) {
			builder.setThemeOverlay(R.style.AppTheme_Amoled);
		}

		if(needMaterial && !useMaterialYou) {
			return true;
		}

		// Build the options
		var options = builder.build();

		// Apply the dynamic colors to the activity
		var activity = (Activity) context;
		DynamicColors.applyToActivityIfAvailable(activity, options);

		if(useOLED) {
			var options2 = new DynamicColorsOptions.Builder()
					.setThemeOverlay(R.style.AppTheme_Amoled)
					.build();

			DynamicColors.applyToActivityIfAvailable(activity, options2);
		}

		return false;
	}

	private boolean isDarkThemeActive(@NonNull Context context) {
		var uiMode = context.getResources().getConfiguration().uiMode;
		return ((uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
	}

	public enum Theme {
		PURPLE("PURPLE"),
		BLUE("BLUE"),
		GREEN("GREEN"),
		PINK("PINK"),
		RED("RED"),
		LAVENDER("LAVENDER"),
		MONOCHROME("MONOCHROME (BETA)"),
		SAIKOU("SAIKOU");

		private final String theme;

		Theme(String theme) {
			this.theme = theme;
		}

		public Theme fromString(String requested) {
			Theme found = null;

			for(var theme : values()) {
				if(!Objects.equals(theme.theme, requested)) continue;
				found = theme;
				break;
			}

			if(found == null) {
				found = PURPLE;
			}

			return found;
		}
	}
}