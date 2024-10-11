package com.mrboomdev.awery.app;

import com.google.android.material.color.DynamicColors;
import com.mrboomdev.awery.BuildConfig;

import org.jetbrains.annotations.NotNull;

public class AweryPlatform {

	public static boolean isRequirementMet(@NotNull String requirement) {
		boolean invert = false;

		if(requirement.startsWith("!")) {
			invert = true;
			requirement = requirement.substring(1);
		}

		var result = switch(requirement) {
			case "material_you" -> DynamicColors.isDynamicColorAvailable();
			case "tv" -> App.isTv();
			case "beta" -> BuildConfig.IS_BETA;
			case "debug" -> BuildConfig.DEBUG;
			case "never" -> false;
			default -> true;
		};

		if(invert) {
			return !result;
		}

		return result;
	}
}