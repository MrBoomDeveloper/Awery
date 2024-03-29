package com.mrboomdev.awery.util.exceptions;

import static com.mrboomdev.awery.app.AweryApp.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.support.js.JsProvider;

import org.mozilla.javascript.ScriptableObject;

import java.util.Collection;

import java9.util.Objects;
import java9.util.stream.Collectors;

/**
 * Can be thrown by {@link JsProvider}
 */
public class JsException extends RuntimeException {
	public static final String ERROR_NOTHING_FOUND = "nothing_found";
	public static final String ERROR_ACCOUNT_REQUIRED = "account_required";
	public static final String ERROR_HTTP = "http_error";
	private final String errorId;
	private final Object errorExtra;

	public JsException(String message) {
		super(message);

		this.errorId = null;
		this.errorExtra = null;
	}

	public JsException(String message, Throwable cause) {
		super(message, cause);

		this.errorId = null;
		this.errorExtra = null;
	}

	public JsException(@NonNull ScriptableObject scope) {
		this.errorId = scope.has("id", scope) ? scope.get("id", scope).toString() : null;
		this.errorExtra = scope.has("extra", scope) ? scope.get("extra", scope) : null;
	}

	public String getErrorId() {
		return errorId;
	}

	public Object getErrorExtra() {
		return errorExtra;
	}

	public boolean isCustom() {
		return errorId != null;
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