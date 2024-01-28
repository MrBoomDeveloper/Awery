package com.mrboomdev.awery.util.graphql;

import androidx.annotation.NonNull;

import java.util.List;

public class GraphQLException extends RuntimeException {

	public GraphQLException(@NonNull GraphQLError error) {
		super(error.toString());
	}

	public GraphQLException(@NonNull List<GraphQLError> errors) {
		super(errors.toString());
	}
}