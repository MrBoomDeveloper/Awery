package com.mrboomdev.awery.data.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import com.squareup.moshi.Moshi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SettingsFactory {

	@NonNull
	public static SettingsItem fromJson(String json) {
		try {
			var moshi = new Moshi.Builder().build();
			var adapter = moshi.adapter(SettingsItem.class);
			return Objects.requireNonNull(adapter.fromJson(json));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NonNull
	public static SettingsItem fromInputStream(InputStream stream) {
		try(var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			var builder = new StringBuilder();
			String line;

			while((line = reader.readLine()) != null) {
				builder.append(line);
			}

			return fromJson(builder.toString());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NonNull
	public static SettingsItem fromAssets(Context context, String file) {
		try(var stream = context.getAssets().open(file)) {
			return fromInputStream(stream);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NonNull
	public static SettingsItem getInstance(Context context) {
		var instance = fromAssets(context, "settings.json");
		instance.setAsParentForChildren();
		instance.restoreValues(AwerySettings.getInstance(context));

		return instance;
	}
}