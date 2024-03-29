package com.mrboomdev.awery.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
	/**
	 * This code is REALLY fucked up.
	 */
	public static String url;

	@Override
	protected void onResume() {
		super.onResume();

		if(url != null) {
			var intent = new Intent();
			intent.putExtra("url", url);
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

		var extras = Objects.requireNonNull(getIntent().getExtras());
		var action = Objects.requireNonNull(extras.getString("action"));

		switch(action) {
			case "open_browser" -> {
				var intent = new Intent(Intent.ACTION_VIEW, Uri.parse(extras.getString("url")));
				startActivity(intent);
			}

			default -> throw new IllegalArgumentException("Unknown login action: " + action);
		}
	}
}