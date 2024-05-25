package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.openUrl;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.Constants;

import org.jetbrains.annotations.Contract;

import xcrash.XCrash;

public class SettingsActions {

	@Contract(pure = true)
	public static void run(@NonNull String actionName) {
		switch(actionName) {
			case "settings_about_app_version" -> {}

			case "settings_player_system_subtitles" -> getAnyContext()
					.startActivity(new Intent(Settings.ACTION_CAPTIONING_SETTINGS));

			case "clear_image_cache" -> {
				var file = getAnyContext().getCacheDir();
				if(file.exists()) file.delete();
			}

			case "settings_about_telegram_group" -> openUrl("https://t.me/mrboomdev_awery");
			case "settings_about_discord_server" -> openUrl("https://discord.gg/yspYzD4Kbm");
			case "settings_about_github_repository" -> openUrl("https://github.com/MrBoomDeveloper/Awery");

			case "settings_advanced_try_crash_native" -> XCrash.testNativeCrash(false);
			case "settings_advanced_try_crash_java" -> XCrash.testJavaCrash(false);

			default -> AweryApp.toast("Unknown action: " + actionName);
		}
	}
}