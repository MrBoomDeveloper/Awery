package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.toast;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class IntentHandlerActivity extends AppCompatActivity {
	private static final String TAG = "IntentHandlerActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		var uri = getIntent().getData();

		if(uri == null) {
			finish();
			return;
		}

		if(Objects.requireNonNull(uri.getPath()).startsWith("/awery/app-login")) {
			toast(uri.toString());
		} else {
			toast(uri.toString());
		}
	}
}