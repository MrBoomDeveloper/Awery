package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.App.openUrl;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.getArgument;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	public static final String ACTION_OPEN_BROWSER = "OPEN_BROWSER";
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_ACTION = "action";
	private static final String SAVED_DID_LAUNCHED_BROWSER = "did_launcher_browser";
	public String completionUrl;
	private boolean didLauncherBrowser, didPause;

	@Override
	protected void onResume() {
		super.onResume();

		if(didLauncherBrowser && didPause) {
			if(completionUrl != null) {
				var intent = new Intent();
				intent.putExtra(EXTRA_URL, completionUrl);
				setResult(Activity.RESULT_OK, intent);
				finish();
			} else {
				toast("You haven't logged it!");
				finish();
			}
		}

		didPause = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		didPause = true;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		var action = requireArgument(getIntent(), EXTRA_ACTION, String.class);

		if(savedInstanceState != null) {
			if(getArgument(savedInstanceState, SAVED_DID_LAUNCHED_BROWSER, Boolean.class)) {
				toast(this, "Failed to login into an account!", 1);
				finish();
				return;
			}
		}

		switch(action) {
			case ACTION_OPEN_BROWSER -> {
				didLauncherBrowser = true;
				openUrl(this, getIntent().getStringExtra(EXTRA_URL));
			}

			default -> throw new IllegalArgumentException("Unknown login action: " + action);
		}
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putBoolean(SAVED_DID_LAUNCHED_BROWSER, requireArgument(
				getIntent(), EXTRA_ACTION, String.class).equals(ACTION_OPEN_BROWSER));

		super.onSaveInstanceState(outState);
	}
}