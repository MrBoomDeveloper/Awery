package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class StringUtil {

	/**
	 * Converts map to json string. If map is null, returns "null".
	 * Used format: {"key1": "value1", "key2": "value2"}
	 * @see #mapToJson(Map)
	 * @param keysInBrackets if true, keys in brackets
	 */
	@NonNull
	public static String mapToJson(@NonNull Map<?, ?> map, boolean keysInBrackets) {
		StringBuilder paramsString = new StringBuilder();

		for(var entry : map.entrySet()) {
			if(paramsString.length() > 0) paramsString.append(", ");

			var stringKey = objectToJsonItem(entry.getKey());

			if(keysInBrackets || stringKey.contains(" ")) {
				paramsString.append("\"");
				paramsString.append(stringKey);
				paramsString.append("\"");
			} else {
				paramsString.append(stringKey.trim());
			}

			paramsString.append(": ");
			paramsString.append(objectToJsonItem(entry.getValue()));
		}

		return paramsString.toString();
	}

	/**
	 * Converts long to string in format: 24:00.
	 */
	@NonNull
	public static String formatTimestamp(long value) {
		if(value < 0) {
			return "00:00";
		}

		value /= 1000;

		var hours = (int) value / 3600;
		var days = hours / 24;

		if(days >= 1) {
			return String.format(Locale.ENGLISH, "%dd %02d:%02d:%02d",
					days, hours % 24, (int) value / 60, (int) value % 60);
		}

		if(hours >= 1) {
			return String.format(Locale.ENGLISH, "%02d:%02d:%02d",
					hours, (int) value / 60, (int) value % 60);
		}

		return String.format(Locale.ENGLISH, "%02d:%02d",
				(int) value / 60, (int) value % 60);
	}

	/**
	 * Parses string to enum. If string is null or enum class is null, returns null.
	 * If string is not a valid enum, returns null.
	 */
	@Contract("null, _ -> null; !null, null -> null")
	@Nullable
	public static <T extends Enum<T>> T parseEnum(String string, Class<T> enumClass) {
		if(string == null || enumClass == null) return null;

		try {
			return T.valueOf(enumClass, string);
		} catch(IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	@NonNull
	public static <T extends Enum<T>> T parseEnum(String string, @NonNull T defaultValue) {
		var result = parseEnum(string, defaultValue.getDeclaringClass());
		return Objects.requireNonNullElse(result, defaultValue);
	}

	public static int parseInteger(String string, int defaultValue) {
		try {
			return Integer.parseInt(string);
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Converts list to unique string.
	 * Used format: ";;;value1;;;value2;;;value3;;;"
	 * @see #uniqueStringToList(String)
	 */
	@NonNull
	public static String listToUniqueString(@NonNull Iterable<String> iterable) {
		var builder = new StringBuilder(";;;");

		for(var string : iterable) {
			builder.append(string).append(";;;");
		}

		return builder.toString();
	}

	/**
	 * Converts unique string to list.
	 * @see #listToUniqueString(Iterable)
	 * @param uniqueString - String of format ";;;value1;;;value2;;;value3;;;";;;
	 */
	@NonNull
	public static @Unmodifiable List<String> uniqueStringToList(@NonNull String uniqueString) {
		return List.of(uniqueString.substring(3, uniqueString.length() - 3).split(";;;"));
	}

	/**
	 * Converts map to json string. If map is null, returns "null".
	 * @see #mapToJson(Map, boolean)
	 * @return String of format "key1": "value1", "key2": "value2"
	 */
	@NonNull
	public static String mapToJson(Map<?, ?> map) {
		return mapToJson(map, false);
	}

	/**
	 * Converts object to json string. If object is null, returns "null".
	 * @return String of format "string", 1, true, 1.0
	 */
	@NonNull
	public static String objectToJsonItem(Object object) {
		if(object == null) {
			return "null";
		}

		String result;

		if(object instanceof Enum<?> enumObject) {
			result = enumObject.name();
		} else {
			result = String.valueOf(object);
		}

		return result;
	}
}