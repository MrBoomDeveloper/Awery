package com.mrboomdev.awery.ui.activity.settings;

import static com.mrboomdev.awery.app.AweryApp.openUrl;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.util.io.FileUtil.deleteFile;

import android.content.Intent;
import android.provider.Settings;

import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.generated.AwerySettings;

import org.jetbrains.annotations.Contract;

import java.io.File;

import xcrash.XCrash;

public class SettingsActions {

	@Contract(pure = true)
	public static void run(String actionName) {
		if(actionName == null) return;

		switch(actionName) {
			case AwerySettings.APP_VERSION -> {}

			case AwerySettings.CLEAR_IMAGE_CACHE -> {
				deleteFile(new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_IMAGE_CACHE));
				toast("Cleared successfully!");
			}

			case AwerySettings.CLEAR_WEBVIEW_CACHE -> {
				deleteFile(new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_WEBVIEW_CACHE));
				toast("Cleared successfully!");
			}

			case AwerySettings.CLEAR_NET_CACHE -> {
				deleteFile(new File(getAnyContext().getCacheDir(), Constants.DIRECTORY_NET_CACHE));
				toast("Cleared successfully!");
			}

			case AwerySettings.PLAYER_SYSTEM_SUBTITLES -> getAnyContext()
					.startActivity(new Intent(Settings.ACTION_CAPTIONING_SETTINGS));

			case AwerySettings.TELEGRAM_GROUP ->
					openUrl("https://t.me/mrboomdev_awery");

			case AwerySettings.DISCORD_SERVER ->
					openUrl("https://discord.com/invite/yspVzD4Kbm");

			case AwerySettings.GITHUB_REPOSITORY ->
					openUrl("https://github.com/MrBoomDeveloper/Awery");

			case AwerySettings.TRY_CRASH_NATIVE ->
					XCrash.testNativeCrash(false);

			case AwerySettings.TRY_CRASH_JAVA ->
					XCrash.testJavaCrash(false);

			default -> toast("Unknown action: " + actionName);
		}
	}
}