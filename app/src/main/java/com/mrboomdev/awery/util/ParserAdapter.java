package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.app.Lifecycle.getAnyActivity;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.app.data.AndroidImage;
import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.ext.data.Image;
import com.mrboomdev.awery.ext.data.Setting;
import com.mrboomdev.awery.ext.data.Settings;
import com.mrboomdev.awery.sdk.util.StringUtils;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.Types;

import org.jetbrains.annotations.Contract;
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
	@FromJson
	public static List<String> listFromString(String value) {
		return new ArrayList<>(StringUtils.uniqueStringToList(value));
	}

	@NonNull
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

	@ToJson
	public static String mapToString(@NonNull Map<String, String> value) {
		return stream(value.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
				.collect(Collectors.joining(",", "{", "}"));
	}

	@ToJson
	public static List<Setting> toJson(Settings list) {
		return list;
	}

	@NonNull
	@Contract("_ -> new")
	@FromJson
	public static Settings toJson(List<Setting> list) {
		return new Settings(list);
	}

	@NonNull
	public static String filtersListToString(SettingsList value) {
		return new Moshi.Builder().build().adapter(SettingsList.class).toJson(value);
	}

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

	@ToJson
	public static String floatLongMapToString(@NonNull Map<Float, Long> value) {
		return stream(value.entrySet())
				.map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
				.collect(Collectors.joining(",", "{", "}"));
	}

	@ToJson
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

	@FromJson
	public Image deserialize(String name) {
		return new AndroidImage(getAnyActivity(AppCompatActivity.class), name);
	}

	@ToJson
	public String serialize(Image image) {
		return ((AndroidImage)image).getRawRes();
	}

	@NonNull
	@FromJson
	public static Calendar calendarFromLong(long millis) {
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar;
	}
}