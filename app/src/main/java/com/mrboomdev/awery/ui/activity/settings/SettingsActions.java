package com.mrboomdev.awery.ui.activity.settings;

import android.content.ClipData;
import android.content.ClipboardManager;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.util.exceptions.ExceptionDetails;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.Contract;

import java.io.IOException;

public class SettingsActions {

	@Contract(pure = true)
	public static void run(@NonNull String actionName) {
		switch(actionName) {
			case "settings_advanced_parse_crash_report" -> {
				var context = AweryApp.getAnyContext();
				var clipboard = context.getSystemService(ClipboardManager.class);
				var read = clipboard.getPrimaryClip();

				if(read == null) {
					AweryApp.toast("Clipboard is empty", 0);
					return;
				}

				var item = read.getItemAt(0);
				var text = item.getText();

				if(text != null) {
					try {
						var moshi = new Moshi.Builder().add(new ExceptionDetails.Adapter()).build();
						var adapter = moshi.adapter(ExceptionDetails.class);
						var details = adapter.fromJson(text.toString());
						if(details == null) return;

						new MaterialAlertDialogBuilder(context)
								.setTitle("Parsed crash report")
								.setMessage(details.toString())
								.setNegativeButton("Copy", (dialog, btn) -> {
									var clip = ClipData.newPlainText("Parsed crash report", details.toString());
									clipboard.setPrimaryClip(clip);
									dialog.dismiss();
								})
								.setPositiveButton("OK", (dialog, btn) -> dialog.dismiss())
								.show();
					} catch(IOException e) {
						e.printStackTrace();
						AweryApp.toast("Failed to parse crash report", 0);
					}
				}
			}
		}
	}
}