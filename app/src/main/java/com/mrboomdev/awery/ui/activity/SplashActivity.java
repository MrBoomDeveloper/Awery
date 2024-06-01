package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.getDatabase;
import static com.mrboomdev.awery.app.CrashHandler.reportIfCrashHappened;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.color.DynamicColors;
import com.mrboomdev.awery.app.CrashHandler;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
	private static final String TAG = "SplashActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		SplashScreen.installSplashScreen(this);
		DynamicColors.applyToActivityIfAvailable(this);
		super.onCreate(savedInstanceState);

		var frame = new LinearLayout(this);
		frame.setGravity(Gravity.CENTER);
		setContentView(frame);

		var loading = new ProgressBar(this);
		frame.addView(loading);

		reportIfCrashHappened(this, () -> new Thread(() -> {
			var db = getDatabase();

			try {
				db.getListDao().getAll();
			} catch(IllegalStateException e) {
				Log.e(TAG, "Failed to test the db!", e);
				CrashHandler.showFatalErrorDialog(this, "Database is corrupted", e);
				return;
			}

			runOnUiThread(() -> {
				var intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
			});
		}).start());
	}
}