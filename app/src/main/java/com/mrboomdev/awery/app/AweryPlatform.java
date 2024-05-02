package com.mrboomdev.awery.app;

import com.google.android.material.color.DynamicColors;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.sdk.PlatformApi;
import com.mrboomdev.awery.sdk.util.FancyVersion;

import org.jetbrains.annotations.NotNull;

public class AweryPlatform extends PlatformApi {
	private static final FancyVersion JVM_LIB_VERSION = new FancyVersion("1.0.0");
	private static final FancyVersion APP_VERSION = new FancyVersion(BuildConfig.VERSION_NAME);

	@Override
	public boolean isRequirementMet(@NotNull String requirement) {
		return switch(requirement) {
			case "material_you" -> DynamicColors.isDynamicColorAvailable();
			case "beta" -> BuildConfig.IS_BETA;
			case "legacy" -> BuildConfig.IS_LEGACY;
			case "debug" -> BuildConfig.DEBUG;
			default -> false;
		};
	}

	@Override
	public FancyVersion getAppVersion() {
		return APP_VERSION;
	}

	@Override
	public @NotNull String getAppName() {
		return "AweryMobile";
	}

	@Override
	public FancyVersion getJvmLibraryVersion() {
		return JVM_LIB_VERSION;
	}

	@Override
	public boolean isBeta() {
		return BuildConfig.IS_BETA;
	}
}