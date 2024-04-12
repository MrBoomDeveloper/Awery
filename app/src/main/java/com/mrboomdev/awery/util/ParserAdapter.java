package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryApp.stream;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.Types;

import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java9.util.stream.Collectors;

@SuppressWarnings("unused")
public class ParserAdapter {
	private static final String TAG = "ParserAdapter";

	@NonNull
	@TypeConverter
	@FromJson
	public static List<String> listFromString(String value) {
		return new ArrayList<>(StringUtil.uniqueStringToList(value));
	}

	@NonNull
	@TypeConverter
	@ToJson
	public static String listToString(@NonNull List<String> value) {
		return StringUtil.listToUniqueString(value);
	}

	@TypeConverter
	@FromJson
	public static Map<String, String> mapFromString(String value) {
		if(value == null) {
			return Collections.emptyMap();
		}

		var moshi = new Moshi.Builder().build();
		var type = Types.newParameterizedType(Map.class, String.class, String.class);
		JsonAdapter<Map<String, String>> adapter = moshi.adapter(type);

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
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
	@FromJson
	public static Map<Float, Long> floatLongMapFromString(String value) {
		if(value == null) {
			return Collections.emptyMap();
		}

		var moshi = new Moshi.Builder().build();
		var type = Types.newParameterizedType(Map.class, String.class, String.class);
		JsonAdapter<Map<Float, Long>> adapter = moshi.adapter(type);

		try {
			return adapter.fromJson(value);
		} catch(IOException e) {
			Log.e(TAG, "Failed to parse string to map", e);
			return Collections.emptyMap();
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
	@FromJson
	@TypeConverter
	public static Calendar calendarFromLong(long millis) {
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar;
	}
}