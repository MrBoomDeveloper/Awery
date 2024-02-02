package com.mrboomdev.awery.data.settings;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SettingsFactory {
	private static SettingsItem instance;

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
			var collector = Collectors.joining(System.lineSeparator());
			var text = reader.lines().collect(collector);
			return fromJson(text);
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
	public static SettingsItem fromFile(File file) {
		try(var stream = new FileInputStream(file)) {
			return fromInputStream(stream);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(@NonNull SettingsItem item, File destination) {
		var json = item.toString();

		try(var stream = new FileOutputStream(destination)) {
			stream.write(json.getBytes(StandardCharsets.UTF_8));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveInstance(Context context) {
		if(instance == null) return;
		save(instance, new File(context.getExternalFilesDir(null),"settings.json"));
	}

	public static SettingsItem getInstance(Context context) {
		if(instance != null) return instance;

		var file = new File(context.getExternalFilesDir(null),"settings.json");
		var predefined = fromAssets(context, "settings.json");

		if(file.exists()) {
			instance = fromFile(file);
			instance.merge(predefined);
		} else {
			instance = predefined;
		}

		instance.setAsParentForChildren();
		saveInstance(context);
		return instance;
	}
}