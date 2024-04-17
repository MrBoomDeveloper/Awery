package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.Constants;

import org.jetbrains.annotations.Contract;

import xcrash.XCrash;

public class SettingsActions {

	@Contract(pure = true)
	public static void run(@NonNull String actionName) {
		switch(actionName) {
			case "clear_cache" -> {
				var file = getAnyContext().getCacheDir();
				if(file.exists()) file.delete();
			}

			case "settings_advanced_try_crash_anr" -> {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch(InterruptedException e) {
						Constants.alwaysThrowException();
					}
				}
			}

			case "settings_advanced_try_crash_native" -> XCrash.testNativeCrash(false);
			case "settings_advanced_try_crash_java" -> XCrash.testJavaCrash(false);

			default -> AweryApp.toast("Unknown action: " + actionName);
		}
	}
}