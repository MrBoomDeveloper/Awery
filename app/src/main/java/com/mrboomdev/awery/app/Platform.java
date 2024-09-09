package com.mrboomdev.awery.app;

import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;

import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.data.settings.SettingsActions;
import com.mrboomdev.awery.ext.data.Setting;

public class Platform extends com.mrboomdev.awery.ext.Platform {

	@Override
	protected void invokeSettingImpl(Setting setting) {
		SettingsActions.run(setting);
	}

	@Override
	protected String getAppNameImpl() {
		return getAnyContext().getString(R.string.app_name);
	}

	@Override
	protected String getAppIdImpl() {
		return BuildConfig.APPLICATION_ID;
	}
}