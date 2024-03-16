package com.mrboomdev.awery.ui.activity.settings;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;

import org.jetbrains.annotations.Contract;

public class SettingsActions {

	@Contract(pure = true)
	public static void run(@NonNull String actionName) {
		switch(actionName) {
			case "clear_cache" -> {
				var file = AweryApp.getAnyContext().getCacheDir();
				if(file.exists()) file.delete();
			}

			default -> AweryApp.toast("Unknown action: " + actionName);
		}
	}
}