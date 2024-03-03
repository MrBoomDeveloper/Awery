package com.mrboomdev.awery.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.splashscreen.SplashScreen;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		SplashScreen.installSplashScreen(this);
		super.onCreate(savedInstanceState);

		var intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}