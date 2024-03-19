package com.mrboomdev.awery.util.graphql;

import androidx.annotation.NonNull;

import java.util.List;

public class GraphQLException extends RuntimeException {
	private final List<GraphQLError> errors;

	public GraphQLException(@NonNull GraphQLError error) {
		super(error.toString());
		this.errors = List.of(error);
	}

	public GraphQLException(@NonNull List<GraphQLError> errors) {
		super(errors.toString());
		this.errors = errors;
	}

	public List<GraphQLError> getGraphQLErrors() {
		return errors;
	}
}