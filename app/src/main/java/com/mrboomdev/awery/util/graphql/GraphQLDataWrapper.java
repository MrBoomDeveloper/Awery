package com.mrboomdev.awery.util.graphql;

import java.util.List;
import java.util.Map;

public class GraphQLDataWrapper<T> {
	private Map<String, T> data;
	private List<GraphQLError> errors;

	@SuppressWarnings("unchecked")
	public T get() {
		checkErrors();
		return (T) data.values().toArray()[0];
	}

	public T get(String key) {
		checkErrors();
		return data.get(key);
	}

	private void checkErrors() {
		if(errors != null && !errors.isEmpty()) {
			if(errors.size() == 1) {
				throw new GraphQLException(errors.get(0));
			}

			throw new GraphQLException(errors);
		}
	}
}