package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public class StringUtil {

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

	@NonNull
	public static String listToUniqueString(@NonNull Iterable<String> iterable) {
		var builder = new StringBuilder(";;;");

		for(var string : iterable) {
			builder.append(string).append(";;;");
		}

		return builder.toString();
	}

	@NonNull
	public static @Unmodifiable List<String> uniqueStringToList(@NonNull String string) {
		return List.of(string.substring(3, string.length() - 3).split(";;;"));
	}

	@NonNull
	public static String mapToJson(Map<?, ?> map) {
		return mapToJson(map, false);
	}

	@NonNull
	public static String objectToJsonItem(Object object) {
		if(object == null) {
			return "null";
		}

		String result;

		if(object instanceof Enum<?> enumObject) {
			result = enumObject.name();
		} else if(object instanceof Integer integerObject) {
			result = String.valueOf(integerObject);
		} else if(object instanceof Boolean booleanObject) {
			result = String.valueOf(booleanObject);
		} else if(object instanceof Float floatValue) {
			result = String.valueOf(floatValue);
		} else {
			result = object.toString();
		}

		return result;
	}
}