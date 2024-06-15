package com.mrboomdev.awery.util.exceptions;

import static com.mrboomdev.awery.extensions.support.js.JsBridge.fromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.isNullJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ParserAdapter.arrayToString;
import static com.mrboomdev.awery.util.ParserAdapter.objectToString;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.extensions.support.js.JsProvider;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.Collection;

import java9.util.Objects;
import java9.util.stream.Collectors;

/**
 * Can be thrown by {@link JsProvider}
 */
public class JsException extends RuntimeException implements LocalizedException, MaybeNotBadException {
	public static final String ERROR_NOTHING_FOUND = "nothing_found";
	public static final String ERROR_ACCOUNT_REQUIRED = "account_required";
	public static final String OTHER = "other";
	public static final String MESSAGE = "message";
	public static final String SERVER_DOWN = "server_down";
	public static final String SERVER_ERROR = "server_error";
	public static final String ERROR_RATE_LIMITED = "rate_limited";
	public static final String ERROR_BANNED = "banned";
	public static final String ERROR_HTTP = "http_error";
	private final Collection<Throwable> errors;
	private final String errorId;
	private final Object errorExtra;

	public JsException(String message) {
		super(message);

		this.errorId = null;
		this.errorExtra = null;
		this.errors = null;
	}

	public JsException(@NonNull ScriptableObject scope) {
		super(scope.has("id", scope) ? scope.get("id", scope).toString() : null);

		this.errorId = stringFromJs(scope.get("id", scope));
		this.errorExtra = fromJs(scope.get("extra", scope), Object.class);

		this.errors = null;
	}

	public JsException(Throwable cause, @NonNull Collection<Throwable> errors) {
		super(stream(errors)
				.map(Throwable::getMessage)
				.filter(Objects::nonNull)
				.collect(Collectors.joining("\n")), cause);

		this.errors = errors;
		this.errorId = null;
		this.errorExtra = null;
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

	public Collection<Throwable> getErrors() {
		return errors;
	}

	@Override
	public String getTitle(Context context) {
		return switch(Objects.requireNonNullElse(errorId, "")) {
			case ERROR_ACCOUNT_REQUIRED -> "Account required";
			case ERROR_HTTP -> "Connection failed";
			case ERROR_NOTHING_FOUND -> context.getString(R.string.nothing_found);
			case ERROR_RATE_LIMITED -> context.getString(R.string.too_much_requests);
			case ERROR_BANNED -> "You are banned!";
			case SERVER_DOWN -> "Server is down!";
			case SERVER_ERROR -> "Server error!";
			case MESSAGE -> "Extension says:";
			case OTHER -> "Extension has crashed!";

			default -> "JsEngine has crashed" + (getErrorId() == null ? "" : ": " + getErrorId());
		};
	}

	@Override
	public String getDescription(Context context) {
		return switch(Objects.requireNonNullElse(errorId, "")) {
			case ERROR_ACCOUNT_REQUIRED -> "You cannot do this action without an account! You can login through extension's settings!";
			case ERROR_HTTP -> "Failed to connect to the server! Try again later.";
			case ERROR_NOTHING_FOUND -> "Nothing was found! Try using other sources.";
			case ERROR_RATE_LIMITED -> "Too much requests in a so short time period.";
			case ERROR_BANNED -> "Well you've made something really bad if they don't want to hear you again.";
			case MESSAGE -> getErrorExtra().toString();

			case OTHER -> {
				if(isNullJs(getErrorExtra())) {
					yield "null";
				}

				if(getErrorExtra() instanceof NativeArray arr) {
					yield arrayToString(arr);
				}

				if(getErrorExtra() instanceof NativeObject obj) {
					yield objectToString(obj);
				}

				if(getErrorExtra() instanceof Throwable t) {
					yield Log.getStackTraceString(t);
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
		if(getErrorId() != null && getErrorId().equals(MESSAGE)) {
			return false;
		}

		return true;
	}

	@Nullable
	@Override
	public String getMessage() {
		if(getErrorId() != null && getErrorId().equals(MESSAGE)) {
			return getErrorExtra().toString();
		}

		return super.getMessage();
	}
}