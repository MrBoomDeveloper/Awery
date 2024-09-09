package com.mrboomdev.awery.ext;

import com.mrboomdev.awery.ext.data.Setting;

import org.jetbrains.annotations.ApiStatus;

public abstract class Platform {
	private static Platform platform;

	protected abstract void invokeSettingImpl(Setting setting);

	protected abstract String getAppNameImpl();

	protected abstract String getAppIdImpl();

	public static String getAppName() {
		return getInstance().getAppNameImpl();
	}

	public static String getAppId() {
		return getInstance().getAppIdImpl();
	}

	@ApiStatus.Internal
	public static Platform getInstance() {
		if(platform != null) {
			return platform;
		}

		throw new IllegalStateException("The platform hasn't been initialized yet!");
	}

	@ApiStatus.Internal
	public static void invokeSetting(Setting setting) {
		getInstance().invokeSettingImpl(setting);
	}

	@ApiStatus.Internal
	public static void setInstance(Platform platform) {
		Platform.platform = platform;
	}
}