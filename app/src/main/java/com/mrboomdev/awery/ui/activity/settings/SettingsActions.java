package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.openUrl;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.util.NiceUtils.clearDirectory;

import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import org.jetbrains.annotations.Contract;

import java.io.File;

import xcrash.XCrash;

public class SettingsActions {

	@Contract(pure = true)
	public static void run(String actionName) {
		if(actionName == null) return;

		switch(actionName) {
			case "settings_about_app_version" -> {}

			case "settings_storage_clear_image_cache" -> {
				var file = new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_IMAGE_CACHE);
				clearDirectory(file);
				toast("Cleared successfully!");
			}

			case "settings_storage_clear_webview_cache" -> {
				var file = new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_WEBVIEW_CACHE);
				clearDirectory(file);
				toast("Cleared successfully!");
			}

			case "settings_storage_clear_net_cache" -> {
				var file = new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_NET_CACHE);
				clearDirectory(file);
				toast("Cleared successfully!");
			}

			case "settings_player_system_subtitles" -> getAnyContext()
					.startActivity(new Intent(Settings.ACTION_CAPTIONING_SETTINGS));

			case "settings_about_telegram_group" ->
					openUrl("https://t.me/mrboomdev_awery");

			case "settings_about_discord_server" ->
					openUrl("https://discord.com/invite/yspVzD4Kbm");

			case "settings_about_github_repository" ->
					openUrl("https://github.com/MrBoomDeveloper/Awery");

			case "settings_advanced_try_crash_native" ->
					XCrash.testNativeCrash(false);

			case "settings_advanced_try_crash_java" ->
					XCrash.testJavaCrash(false);

			default -> toast("Unknown action: " + actionName);
		}
	}
}