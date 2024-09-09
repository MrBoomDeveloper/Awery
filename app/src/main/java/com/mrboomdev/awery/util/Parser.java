package com.mrboomdev.awery.util;

import static java.util.Objects.requireNonNull;

import androidx.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;

public class Parser {
	private static final Moshi moshi = new Moshi.Builder().add(new ParserAdapter()).build();

	@NonNull
	public static String toString(Type type, Object object) {
		return moshi.adapter(type).toJson(object);
	}

	@NonNull
	public static <T> String toString(Class<T> type, T object) {
		return moshi.adapter(type).toJson(object);
	}

	@NonNull
	public static <T> String toString(@NonNull Adapter<T> adapter, T object) {
		return adapter.adapter.toJson(object);
	}

	@NonNull
	@SuppressWarnings("unchecked")
	public static <T> T fromString(Type type, String json) throws IOException {
		return (T) requireNonNull(moshi.adapter(type).fromJson(json));
	}

	@NonNull
	public static <T> T fromString(Class<T> type, String json) throws IOException {
		return requireNonNull(moshi.adapter(type).fromJson(json));
	}

	@NonNull
	public static <T> T fromString(@NonNull Adapter<T> adapter, String json) throws IOException {
		return requireNonNull(adapter.adapter.fromJson(json));
	}

	@NonNull
	@SuppressWarnings("unchecked")
	public static <T> Adapter<T> getAdapter(@NonNull Class<?> type, Class<?>... generics) {
		if(generics != null && generics.length > 0) {
			return new Adapter<>(moshi.adapter(Types.newParameterizedType(type, generics)));
		}

		return (Adapter<T>) new Adapter<>(moshi.adapter(type));
	}

	public static class Adapter<T> {
		private final JsonAdapter<T> adapter;

		private Adapter(JsonAdapter<T> adapter) {
			this.adapter = adapter;
		}
	}
}