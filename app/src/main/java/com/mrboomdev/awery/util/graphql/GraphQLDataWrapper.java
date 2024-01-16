package com.mrboomdev.awery.util.graphql;

import java.util.Map;

public class GraphQLDataWrapper<T> {
	private final Map<String, T> data;

	public GraphQLDataWrapper(Map<String, T> data) {
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	public T get() {
		return (T) data.values().toArray()[0];
	}

	public T get(String key) {
		return data.get(key);
	}
}