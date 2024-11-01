package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.listToUniqueString;
import static com.mrboomdev.awery.util.NiceUtils.parseDate;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.NiceUtils.uniqueStringToList;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import com.mrboomdev.awery.app.data.settings.SettingsItem;
import com.mrboomdev.awery.app.data.settings.SettingsList;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.Types;

import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java9.util.stream.Collectors;

@Deprecated(forRemoval = true)
@SuppressWarnings("unused")
public class ParserAdapter {
	private static final String TAG = "ParserAdapter";

	@NonNull
	@TypeConverter
	@FromJson
	public static List<String> listFromString(String value) {
		return new ArrayList<>(uniqueStringToList(value));
	}

	@NonNull
	@TypeConverter
	@ToJson
	public static String listToString(@NonNull List<String> value) {
		return listToUniqueString(value);
	}

	@TypeConverter
	@FromJson
	public static Map<String, String> mapFromString(String value) {
		if(value == null) {
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
	@ToJson
	public static String mapToString(@NonNull Map<String, String> value) {
		return stream(value.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
				.collect(Collectors.joining(",", "{", "}"));
	}

	@TypeConverter
	public static SettingsList filtersListFromString(String value) {
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

	@ToJson
	public static List<SettingsItem> toJson(SettingsList list) {
		return list;
	}

	@NonNull
	@Contract("_ -> new")
	@FromJson
	public static SettingsList toJson(List<SettingsItem> list) {
		return new SettingsList(list);
	}

	@NonNull
	@TypeConverter
	public static String filtersListToString(SettingsList value) {
		return new Moshi.Builder().build().adapter(SettingsList.class).toJson(value);
	}

	@TypeConverter
	@FromJson
	public static Map<Float, Long> floatLongMapFromString(String value) {
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

	@ToJson
	public static String toJson(Throwable serializable) throws IOException {
		try(var arrayStream = new ByteArrayOutputStream(); var outputStream = new ObjectOutputStream(arrayStream)) {
			outputStream.writeObject(serializable);
			return Base64.encodeToString(arrayStream.toByteArray(), Base64.DEFAULT);
		}
	}

	@FromJson
	public static Throwable fromJson(@NonNull String string) throws IOException, ClassNotFoundException {
		var data = Base64.decode(string, Base64.DEFAULT);

		try(var stream = new ObjectInputStream(new ByteArrayInputStream(data))) {
			return (Throwable) stream.readObject();
		}
	}

	@TypeConverter
	@ToJson
	public static String floatLongMapToString(@NonNull Map<Float, Long> value) {
		return stream(value.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
				.collect(Collectors.joining(",", "{", "}"));
	}

	@ToJson
	@TypeConverter
	public static long calendarToLong(@NonNull Calendar calendar) {
		return calendar.getTimeInMillis();
	}

	@NonNull
	public static Calendar calendarFromString(String date) {
		var calendar = Calendar.getInstance();
		calendar.setTime(parseDate(date));
		return calendar;
	}

	@NonNull
	public static Calendar calendarFromNumber(@NonNull Number millis) {
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis.longValue());
		return calendar;
	}

	@NonNull
	@FromJson
	@TypeConverter
	public static Calendar calendarFromLong(long millis) {
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar;
	}
}