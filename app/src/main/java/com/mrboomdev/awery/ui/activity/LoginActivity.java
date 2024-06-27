package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.util.NiceUtils.requireArgument;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	public static final String ACTION_OPEN_BROWSER = "OPEN_BROWSER";
	public static final String EXTRA_URL = "url";
	/**
	 * This code is REALLY fucked up.
	 */
	public static String url;

	@Override
	protected void onResume() {
		super.onResume();

		if(url != null) {
			var intent = new Intent();
			intent.putExtra(EXTRA_URL, url);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		url = null;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		url = null;
		super.onCreate(savedInstanceState);

		switch(requireArgument(getIntent(), "action", String.class)) {
			case ACTION_OPEN_BROWSER -> {
				var uri = Uri.parse(getIntent().getStringExtra(EXTRA_URL));
				var intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}

			default -> throw new IllegalArgumentException(
					"Unknown login action: " + requireArgument(
							getIntent(), "action", String.class));
		}
	}
}