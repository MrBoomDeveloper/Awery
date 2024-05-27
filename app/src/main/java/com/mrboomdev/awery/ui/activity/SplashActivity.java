package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.CrashHandler.reportIfCrashHappened;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.color.DynamicColors;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		SplashScreen.installSplashScreen(this);
		DynamicColors.applyToActivityIfAvailable(this);
		super.onCreate(savedInstanceState);

		reportIfCrashHappened(this, () -> {
			var intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		});
	}
}