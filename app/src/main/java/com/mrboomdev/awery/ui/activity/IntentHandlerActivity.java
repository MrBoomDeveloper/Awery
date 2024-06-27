package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.services.BackupService;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.io.FileUtil;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.util.Objects;

public class IntentHandlerActivity extends AppCompatActivity {
	private static final String TAG = "IntentHandlerActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		super.onCreate(savedInstanceState);

		var uri = getIntent().getData();

		if(uri == null) {
			finish();
			return;
		}

		if(Objects.requireNonNull(uri.getPath()).startsWith("/awery/app-login/")) {
			LoginActivity.url = uri.toString();

			var intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);

			finish();
		} else if(returnWith(FileUtil.getUriFileName(uri), name -> (name != null && name.endsWith(".awerybck")))) {
			new DialogBuilder(this)
					.setTitle(R.string.restore_backup)
					.setMessage("Are you sure want to restore an saved backup? All your current data will be erased!")
					.setNegativeButton(R.string.cancel, dialog -> finish())
					.setPositiveButton(R.string.confirm, dialog -> {
						dialog.dismiss();

						var backupIntent = new Intent(this, BackupService.class);
						backupIntent.setAction(BackupService.ACTION_RESTORE);
						backupIntent.setData(uri);

						if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) startService(backupIntent);
						else startForegroundService(backupIntent);
					}).show();
		} else {
			toast("Unknown intent!", 1);
			finish();
		}
	}
}