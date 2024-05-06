package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import com.mrboomdev.awery.sdk.util.StringUtils;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.Types;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

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

@SuppressWarnings("unused")
public class ParserAdapter {
	private static final String TAG = "ParserAdapter";

	@NonNull
	@TypeConverter
	@FromJson
	public static List<String> listFromString(String value) {
		return new ArrayList<>(StringUtils.uniqueStringToList(value));
	}

	@NonNull
	@TypeConverter
	@ToJson
	public static String listToString(@NonNull List<String> value) {
		return StringUtils.listToUniqueString(value);
	}

	public static String arrayToString(NativeArray array) {
		if(array == null) return null;

		var builder = new StringBuilder();
		var iterator = array.iterator();

		while(iterator.hasNext()) {
			var item = iterator.next();

			if(item instanceof NativeArray arr) {
				builder.append(arrayToString(arr));
			} else if(item instanceof NativeObject obj) {
				builder.append(objectToString(obj));
			} else {
				builder.append(item.toString());
			}

			if(iterator.hasNext()) {
				builder.append(", ");
			}
		}

		return "[ " + builder + " ]";
	}

	public static String objectToString(NativeObject object) {
		if(object == null) return null;

		var builder = new StringBuilder();
		var iterator = object.entrySet().iterator();

		while(iterator.hasNext()) {
			var entry = iterator.next();

			builder.append("\"")
					.append(entry.getKey())
					.append("\":");

			if(entry.getValue() instanceof NativeArray arr) {
				builder.append(arrayToString(arr));
			} else if(entry.getValue() instanceof NativeObject obj) {
				builder.append(objectToString(obj));
			} else {
				builder.append(entry.getValue().toString());
			}

			if(iterator.hasNext()) {
				builder.append(", ");
			}
		}

		return "{" + builder + "}";
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
	@FromJson
	public static Map<Float, Long> floatLongMapFromString(String value) {
		if(value == null) {
			return Collections.emptyMap();
		}

		var moshi = new Moshi.Builder().build();
		var type = Types.newParameterizedType(Map.class, Float.class, Long.class);
		JsonAdapter<Map<Float, Long>> adapter = moshi.adapter(type);

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
		calendar.setTime(StringUtils.parseDate(date));
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