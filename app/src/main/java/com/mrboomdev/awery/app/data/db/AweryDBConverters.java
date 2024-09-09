package com.mrboomdev.awery.app.data.db;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.ext.data.ExternalService;
import com.mrboomdev.awery.ext.data.ImageType;
import com.mrboomdev.awery.ext.data.User;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java9.util.stream.Collectors;

@SuppressWarnings("unused")
class AweryDBConverters {
	private static final String TAG = "AweryDBConverters";

	@NonNull
	@TypeConverter
	public List<String> deserializeStringList(String value) {
		return new ArrayList<>(StringUtils.uniqueStringToList(value));
	}

	@NonNull
	@TypeConverter
	public String[] deserializeStringArray(String value) {
		return StringUtils.uniqueStringToList(value).toArray(new String[0]);
	}

	@TypeConverter
	public User[] deserializeUsersArray(String value) {
		if(value == null || value.isBlank()) {
			return new User[0];
		}

		var adapter = new Moshi.Builder().build().adapter(User[].class);

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
			toast("Your data has been corrupted! Sorry, but we can't do anything with it :(");
			Log.e(TAG, "Failed to parse string to array", e);
			return new User[0];
		}
	}

	@TypeConverter
	public String serializeUsersArray(User[] users) {
		if(users == null || users.length == 0) {
			return null;
		}

		return new Moshi.Builder().build()
				.adapter(User[].class)
				.toJson(users);
	}

	@TypeConverter
	public ExternalService[] deserializeExternalServicesArray(String value) {
		if(value == null || value.isBlank()) {
			return new ExternalService[0];
		}

		var adapter = new Moshi.Builder().build().adapter(ExternalService[].class);

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
			toast("Your data has been corrupted! Sorry, but we can't do anything with it :(");
			Log.e(TAG, "Failed to parse string to array", e);
			return new ExternalService[0];
		}
	}

	@TypeConverter
	public String serializeExternalServicesArray(ExternalService[] users) {
		if(users == null || users.length == 0) {
			return null;
		}

		return new Moshi.Builder().build()
				.adapter(ExternalService[].class)
				.toJson(users);
	}

	@NonNull
	@TypeConverter
	public String serializeStringArray(String[] value) {
		return StringUtils.listToUniqueString(List.of(value));
	}

	@NonNull
	@TypeConverter
	public String serializeStringList(@NonNull List<String> value) {
		return StringUtils.listToUniqueString(value);
	}

	@TypeConverter
	public Map<String, String> deserializeStringMap(String value) {
		if(value == null || value.isBlank()) {
			return Collections.emptyMap();
		}

		var adapter = new Moshi.Builder().build().<Map<String, String>>
				adapter(Types.newParameterizedType(Map.class, String.class, String.class));

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
			toast("Your data has been corrupted! Sorry, but we can't do anything with it :(");
			Log.e(TAG, "Failed to parse string to map", e);
			return Collections.emptyMap();
		}
	}

	@TypeConverter
	public String serializeStringMap(@NonNull Map<String, String> value) {
		return stream(value.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
				.collect(Collectors.joining(",", "{", "}"));
	}

	@TypeConverter
	public SettingsList deserializeSettingsList(String value) {
		if(value == null) {
			return new SettingsList();
		}

		var adapter = new Moshi.Builder().build().adapter(SettingsList.class);

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
			toast("Your data has been corrupted! Sorry, but we can't do anything with it :(");
			Log.e(TAG, "Failed to parse string to map", e);
			return new SettingsList();
		}
	}

	@NonNull
	@TypeConverter
	public String serializeSettingsList(SettingsList value) {
		return new Moshi.Builder().build().adapter(SettingsList.class).toJson(value);
	}

	@TypeConverter
	public Map<Float, Long> deserializeFloatLongMap(String value) {
		if(value == null) {
			return Collections.emptyMap();
		}

		var adapter = new Moshi.Builder().build().<Map<Float, Long>>
				adapter(Types.newParameterizedType(Map.class, Float.class, Long.class));

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
			toast("Your data has been corrupted! Sorry, but we can't do anything with it :(");
			Log.e(TAG, "Failed to parse string to map", e);
			return Collections.emptyMap();
		}
	}

	@TypeConverter
	public String serializeLong(Long value) {
		return value != null ? value.toString() : null;
	}

	@TypeConverter
	public Long deserializeLong(String string) {
		return (string == null || string.isBlank()) ? null : Long.valueOf(string);
	}

	@TypeConverter
	public Map<ImageType, String> deserializeImageTypeStringMap(String value) {
		if(value == null) {
			return Collections.emptyMap();
		}

		var adapter = new Moshi.Builder().build().<Map<ImageType, String>>
				adapter(Types.newParameterizedType(Map.class, ImageType.class, String.class));

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
			toast("Your data has been corrupted! Sorry, but we can't do anything with it :(");
			Log.e(TAG, "Failed to parse string to map", e);
			return Collections.emptyMap();
		}
	}

	@TypeConverter
	public String serializeFloatLongMap(@NonNull Map<Float, Long> value) {
		return stream(value.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
				.collect(Collectors.joining(",", "{", "}"));
	}

	@TypeConverter
	public String serializeImageTypeStringMap(@NonNull Map<ImageType, String> value) {
		return stream(value.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
				.collect(Collectors.joining(",", "{", "}"));
	}

	@TypeConverter
	public long serializeDate(@NonNull Calendar calendar) {
		return calendar.getTimeInMillis();
	}

	@NonNull
	@TypeConverter
	public Calendar deserializeDate(long millis) {
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar;
	}
}