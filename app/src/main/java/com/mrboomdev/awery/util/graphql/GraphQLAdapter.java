package com.mrboomdev.awery.util.graphql;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

public class GraphQLAdapter<T> {
	private static final Moshi moshi = new Moshi.Builder().build();
	private Type type;
	private Class<?> clazz;

	public GraphQLAdapter(Type type) {
		this.type = type;
	}

	public GraphQLAdapter(Class<?> clazz) {
		this.clazz = clazz;
	}

	public GraphQLDataWrapper<T> parse(String json) throws IOException {
		var wrapperType = GraphQLParser.getTypeWithGenerics(
				GraphQLDataWrapper.class, ((clazz != null) ? clazz : type));

		GraphQLDataWrapper<T> result = null;

		try {
			var adapter = moshi.<GraphQLDataWrapper<T>>adapter(wrapperType);
			result = adapter.fromJson(json);
		} catch(IOException e) {
			throw new IOException("Failed to parse with type " + wrapperType, e);
		}

		if(result == null) {
			throw new IllegalArgumentException("Json was null!");
		}

		return result;
	}

	public T parseFirst(String json) throws IOException {
		return parse(json).get();
	}
}