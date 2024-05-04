package com.mrboomdev.awery.util.graphql;

import androidx.annotation.NonNull;

import com.squareup.moshi.Types;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class GraphQLParser {

	@NonNull
	@Contract("_, null -> new")
	public static <T> GraphQLAdapter<T> createAdapter(Class<?> clazz, Class<?>... genericClasses) {
		if(genericClasses != null && genericClasses.length > 0) {
			var type = getTypeWithGenerics(clazz, genericClasses);
			return new GraphQLAdapter<>(type);
		}

		return new GraphQLAdapter<>(clazz);
	}

	@NonNull
	public static <T> List<T> parseList(String json, Class<T> clazz) throws IOException {
		GraphQLAdapter<List<T>> adapter = createAdapter(List.class, clazz);
		return adapter.parseFirst(json);
	}

	@NonNull
	public static <T> T parse(String json, @NonNull Class<T> clazz, Class<?>... genericClasses) throws IOException {
		GraphQLAdapter<T> adapter = createAdapter(clazz, genericClasses);
		return adapter.parseFirst(json);
	}

	@NonNull
	@Contract("_, _ -> new")
	public static Type getTypeWithGenerics(Type firstClazz, Type... otherClasses) {
		return Types.newParameterizedType(firstClazz, otherClasses);
	}
}