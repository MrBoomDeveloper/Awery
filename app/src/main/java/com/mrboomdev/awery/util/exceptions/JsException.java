package com.mrboomdev.awery.util.exceptions;

import static com.mrboomdev.awery.app.App.i18n;
import static com.mrboomdev.awery.util.NiceUtils.deserialize;
import static com.mrboomdev.awery.util.NiceUtils.nonNullElse;
import static com.mrboomdev.awery.util.ParserAdapter.arrayToString;
import static com.mrboomdev.awery.util.ParserAdapter.objectToString;

import android.util.Log;

import androidx.annotation.Nullable;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueString;
import com.caoccao.javet.values.reference.V8ValueError;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.mrboomdev.awery.R;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import java9.util.Objects;

public class JsException extends RuntimeException implements LocalizedException, MaybeNotBadException {
	public static final String SERIALIZED_EXCEPTION = "__SERIALIZED_E__";
	public static final String ERROR_NOTHING_FOUND = "NOTHING_FOUND";
	public static final String ERROR_ACCOUNT_REQUIRED = "ACCOUNT_REQUIRED";
	public static final String OTHER = "OTHER";
	public static final String MESSAGE = "MESSAGE";
	public static final String SERVER_DOWN = "SERVER_DOWN";
	public static final String SERVER_ERROR = "SERVER_ERROR";
	public static final String ERROR_RATE_LIMITED = "RATE_LIMITED";
	public static final String ERROR_BANNED = "BANNED";
	public static final String ERROR_HTTP = "HTTP_ERROR";
	private static final String TAG = "JsException";
	private final Collection<Throwable> errors;
	private final String errorId;
	private final Object extra;

	public JsException(String message) {
		super(message);
		this.errorId = null;
		this.extra = message;
		this.errors = null;
	}

	public JsException(V8Value v8Value) throws JavetException {
		// Fuck Java! Why can't i call super later!?
		super(getMessage(v8Value));

		try {
			initCause(v8Value instanceof V8ValueObject o
					? (o.get(SERIALIZED_EXCEPTION) instanceof V8ValueString to
					? (Throwable) deserialize(to.asString()) : null) : null);
		} catch(JavetException | IOException | ClassNotFoundException e) {
			Log.e(TAG, "Failed to deserialize an actual exception!", e);
		}

		if(v8Value instanceof V8ValueError error) {
			this.errorId = OTHER;
			this.extra = error.getStack();
			this.errors = List.of(new JsException(error.getStack()));
		} else if(v8Value instanceof V8ValueObject o) {
			this.errorId = o.get("id").asString();

			if(getCause() != null) {
				this.extra = getCause();
			} else {
				this.extra = o.get("extra").asString();
			}

			this.errors = null;
		} else {
			this.errorId = null;
			this.extra = null;
			this.errors = null;
		}
	}

	private static String getMessage(V8Value v8Value) throws JavetException {
		if(v8Value == null || v8Value.isNullOrUndefined()) {
			return null;
		}

		if(v8Value instanceof V8ValueError error) {
			return "\"" + error.getErrorType() + "\" from an JS engine\n" + error.getStack();
		}

		if(v8Value instanceof V8ValueObject o) {
			return o.has("id") ? o.get("id").asString() : null;
		}

		return v8Value.asString();
	}

	public String getErrorId() {
		return errorId;
	}

	public Object getErrorExtra() {
		return extra;
	}

	public Collection<Throwable> getErrors() {
		return errors;
	}

	@Override
	public String getTitle() {
		return switch(Objects.requireNonNullElse(errorId, "")) {
			case ERROR_ACCOUNT_REQUIRED -> "Account required";
			case ERROR_HTTP -> "Connection failed";
			case ERROR_NOTHING_FOUND -> i18n(R.string.nothing_found);
			case ERROR_RATE_LIMITED -> i18n(R.string.too_much_requests);
			case ERROR_BANNED -> "You are banned!";
			case SERVER_DOWN -> i18n(R.string.server_down);
			case SERVER_ERROR -> "Server error!";
			case MESSAGE -> "Extension says:";
			case OTHER -> "Extension has crashed!";

			default -> "Extension has crashed! " + (getErrorId() == null ? "" : getErrorId());
		};
	}

	@Override
	public String getLocalizedMessage() {
		return switch(nonNullElse(errorId, "")) {
			case ERROR_ACCOUNT_REQUIRED -> "You cannot do this action without an account! You can login through extension's settings!";
			case ERROR_HTTP -> "Failed to connect to the server! Try again later.";
			case ERROR_NOTHING_FOUND -> "Nothing was found! Try using other sources.";
			case ERROR_RATE_LIMITED -> "Too much requests in a so short time period.";
			case ERROR_BANNED -> "Well you've made something really bad if they don't want to hear you again.";
			case MESSAGE -> getErrorExtra().toString();

			case OTHER -> {
				if(extra == null || (extra instanceof V8Value v8Value && v8Value.isNullOrUndefined())) {
					yield "null";
				}

				if(getErrorExtra() instanceof NativeArray arr) {
					yield arrayToString(arr);
				}

				if(getErrorExtra() instanceof NativeObject obj) {
					yield objectToString(obj);
				}

				if(getErrorExtra() instanceof Throwable t) {
					yield ExceptionDescriptor.getMessage(t);
				}

				yield getErrorExtra().toString();
			}

			default -> {
				var extra = getErrorExtra();

				if(extra != null) {
					yield extra.toString();
				}

				if(getErrors() != null) {
					var iterator = getErrors().iterator();
					var builder = new StringBuilder();

					while(iterator.hasNext()) {
						var error = iterator.next();
						var message = error.getMessage();

						if(message == null) {
							continue;
						}

						builder.append(message.trim());

						if(iterator.hasNext()) {
							builder.append("\n\n");
						}
					}

					yield builder.toString();
				}

				yield errorId;
			}
		};
	}

	@Override
	public boolean isBad() {
		return getErrorId() == null || !getErrorId().equals(MESSAGE);
	}

	@Nullable
	@Override
	public String getMessage() {
		if(MESSAGE.equals(getErrorId())) {
			return getErrorExtra().toString();
		}

		return super.getMessage();
	}
}