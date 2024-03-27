package com.mrboomdev.awery.util.exceptions;

import static com.mrboomdev.awery.app.AweryApp.stream;

import androidx.annotation.NonNull;

import java.util.Collection;

import java9.util.Objects;
import java9.util.stream.Collectors;

public class JsException extends RuntimeException {

	public JsException(String message) {
		super(message);
	}

	public JsException(String message, Throwable cause) {
		super(message, cause);
	}

	@NonNull
	public static JsException create(Throwable cause, @NonNull Collection<Throwable> errors) {
		var message = stream(errors)
				.map(Throwable::getMessage)
				.filter(Objects::nonNull)
				.collect(Collectors.joining("\n"));

		return new JsException(message, cause);
	}
}