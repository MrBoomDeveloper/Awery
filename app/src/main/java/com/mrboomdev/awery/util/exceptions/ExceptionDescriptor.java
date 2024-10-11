package com.mrboomdev.awery.util.exceptions;

import android.content.Context;
import android.os.Build;
import android.os.strictmode.InstanceCountViolation;
import android.os.strictmode.NetworkViolation;
import android.os.strictmode.Violation;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;

import org.jetbrains.annotations.Contract;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import eu.kanade.tachiyomi.network.HttpException;
import java9.util.Objects;
import kotlinx.serialization.SerializationException;

public class ExceptionDescriptor {
	private static final String ROOM_EXCEPTION = "Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number.";
	private final Throwable throwable;

	public ExceptionDescriptor(@NonNull Throwable t) {
		this.throwable = unwrap(t);
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public static Throwable unwrap(Throwable t) {
		if(!isUnknownException(t)) {
			return t;
		}

		var firstCause = t.getCause();

		if(firstCause != null) {
			var causes = new ArrayList<Throwable>();
			var currentCause = firstCause;

			while(currentCause != null) {
				causes.add(currentCause);
				currentCause = currentCause.getCause();
			}

			for(int i = causes.size() - 1; i >= 0; i--) {
				var cause = causes.get(i);
				if(cause == firstCause) break;

				if(!isUnknownException(cause)) {
					return cause;
				}
			}
		}

		return t;
	}

	public static String print(Throwable t, Context context) {
		var title = getTitle(t, context);
		var message = getMessage(t, context);

		if(title == null) return message;
		if(message == null) return title;

		if(Objects.equals(title, message)) {
			return title;
		}

		return title + "\n" + message;
	}

	public static String getTitle(Throwable t, Context context) {
		if(t instanceof LocalizedException e) {
			return e.getTitle(context);
		} else if(t instanceof UnimplementedException
				|| t instanceof UnsupportedOperationException) {
			return context.getString(R.string.not_implemented);
		} else if(t instanceof SocketTimeoutException) {
			return context.getString(R.string.timed_out);
		} else if(t instanceof BotSecurityBypassException e) {
			return "Failed to bypass " + e.getBlockerName();
		} else if(t instanceof ExtensionNotInstalledException) {
			return "Extension not installed";
		} else if(t instanceof SocketException e) {
			return e.getMessage();
		} else if(t instanceof CancelledException) {
			return t.getMessage();
		} else if(t instanceof SSLHandshakeException) {
			return context.getString(R.string.failed_handshake);
		} else if(t instanceof HttpException e) {
			return getHttpErrorTitle(context, e.getCode());
		} else if(t instanceof NullPointerException) {
			return "Parser is broken!";
		} else if(t instanceof UnknownHostException) {
			return t.getMessage();
		} else if(t instanceof SerializationException) {
			return "Parser has crashed!";
		} else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && t instanceof Violation) {
			return "Bad thing has happened...";
		}

		if(t.getMessage() != null && t.getMessage().contains(ROOM_EXCEPTION)) {
			return "Database corrupted!";
		}

		return getGenericTitle(context);
	}

	public String getTitle(Context context) {
		return getTitle(throwable, context);
	}

	public static Reason getReason(Throwable t) {
		for(var reason : Reason.values()) {
			if(reason == Reason.OTHER) {
				continue;
			}

			if(reason.isMe(t)) {
				return reason;
			}
		}

		return Reason.OTHER;
	}

	public Reason getReason() {
		return getReason(throwable);
	}

	public boolean isNetworkException() {
		return throwable instanceof ZeroResultsException ||
				throwable instanceof UnimplementedException ||
				throwable instanceof SocketTimeoutException ||
				throwable instanceof SocketException ||
				throwable instanceof HttpException ||
				throwable instanceof SSLHandshakeException;
	}

	public static boolean isUnknownException(Throwable t) {
		return !(t instanceof ZeroResultsException ||
				t instanceof UnimplementedException ||
				t instanceof SocketTimeoutException ||
				t instanceof ExtensionNotInstalledException ||
				t instanceof SocketException ||
				t instanceof SSLHandshakeException ||
				t instanceof HttpException ||
				t instanceof UnsupportedOperationException ||
				t instanceof SerializationException ||
				t instanceof UnknownHostException);
	}

	@NonNull
	@Contract(pure = true)
	private static String getGenericTitle(@NonNull Context context) {
		return context.getString(R.string.something_went_wrong);
	}

	@NonNull
	private static String getGenericMessage(Throwable throwable) {
		return Log.getStackTraceString(throwable);
	}

	public String getMessage(Context context) {
		return getMessage(throwable, context);
	}

	public static String getMessage(@NonNull Throwable t, Context context) {
		if(t instanceof LocalizedException e) {
			return e.getDescription(context);
		} else if(t instanceof UnimplementedException) {
			return t.getMessage();
		} else if(t instanceof ExtensionNotInstalledException e) {
			return "Please check your filters again. Maybe used extension was removed. It's id: " + e.getExtensionName();
		} else if(t instanceof BotSecurityBypassException) {
			return "Try opening the website and completing their bot check.";
		} else if(t instanceof SocketTimeoutException) {
			return context.getString(R.string.connection_timeout);
		} else if(t instanceof SocketException || t instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(t instanceof HttpException e) {
			return getHttpErrorMessage(context, e.getCode());
		} else if(t instanceof UnknownHostException e) {
			return e.getMessage();
		} else if(t instanceof SerializationException) {
			return "An error has occurred while parsing the response. " + t.getMessage();
		} else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && t instanceof Violation) {
			if(t instanceof InstanceCountViolation) {
				return "Too much instances of the object was created. " + t.getMessage();
			} else if(t instanceof NetworkViolation) {
				return "You can't run network requests at the ui thread!";
			}

			return getGenericMessage(t);
		}

		if(t.getMessage() != null && t.getMessage().contains(ROOM_EXCEPTION)) {
			return "Yeah, you've hear right. The database has been corrupted!" +
					"\nHow can you fix it? Clear app data." +
					"\n\nPlease, do not use alpha versions to keep your library. Use them only to test new things." +
					"\n\n" + getGenericMessage(t);
		}

		return getGenericMessage(t);
	}

	@NonNull
	private static String getHttpErrorTitle(Context context, int code) {
		return switch(code) {
			case 400, 422 -> "Bad request";
			case 403 -> context.getString(R.string.access_denied);
			case 404 -> context.getString(R.string.nothing_found);
			case 429 -> context.getString(R.string.too_much_requests);
			case 500, 503 -> context.getString(R.string.server_down);
			case 504 -> context.getString(R.string.timed_out);
			default -> "Unknown network error";
		};
	}

	@NonNull
	private static String getHttpErrorMessage(Context context, int code) {
		return "(" + code + ") " + switch(code) {
			case 400, 422 -> "The request was invalid, please try again later.";
			case 401 -> "You are not logged in, please log in and try again.";
			case 403 -> "You have no access to this resource, try logging into your account.";
			case 404 -> context.getString(R.string.not_found_detailed);
			case 429 -> "You have exceeded the rate limit, please try again later.";
			case 500 -> "An internal server error has occurred, please try again later.";
			case 503 -> "The server is currently unavailable, please try again later.";
			case 504 -> "The connection timed out, please try again later.";
			default ->  context.getString(R.string.unknown_error);
		};
	}

	@NonNull
	@Override
	public String toString() {
		return Log.getStackTraceString(throwable);
	}

	public enum Reason {
		SERVER_DOWN {
			@Override
			protected boolean isMeImpl(Throwable t) {
				if(t instanceof HttpException e) {
					return switch(e.getCode()) {
						case 500, 503 -> true;
						default -> false;
					};
				}

				return false;
			}
		},
		/*BAD_NETWORK {

		},
		NO_ACCESS {

		},
		BANNED {

		},
		ACCOUNT_REQUIRED {

		},
		BAD_CODE {

		},
		NOTHING_FOUND {

		},
		ILLEGAL_QUERY {

		},
		TIMEOUT {

		},
		RATE_LIMITED {

		},*/
		UNIMPLEMENTED {
			@Override
			protected boolean isMeImpl(Throwable t) {
				return t instanceof UnsupportedOperationException || t instanceof UnimplementedException;
			}
		},
		OTHER {
			@Override
			protected boolean isMeImpl(Throwable t) {
				throw new UnsupportedOperationException("You must avoid calling this method on OTHER");
			}
		};

		public boolean isMe(Throwable t) {
			return isMeImpl(unwrap(t));
		}

		/*
		if(throwable instanceof JsException js) {
			return switch(Objects.requireNonNullElse(js.getErrorId(), "")) {
				case JsException.ERROR_RATE_LIMITED -> Reason.RATE_LIMITED;
				case JsException.SERVER_ERROR, JsException.SERVER_DOWN -> Reason.SERVER_DOWN;
				case JsException.ERROR_ACCOUNT_REQUIRED -> Reason.NO_ACCESS;
				case JsException.ERROR_HTTP -> Reason.BAD_NETWORK;
				case JsException.ERROR_NOTHING_FOUND -> Reason.NOTHING_FOUND;
				case JsException.ERROR_BANNED -> Reason.BANNED;
				default -> Reason.OTHER;
			};
		}

		if(throwable instanceof ZeroResultsException) {
			return Reason.NOTHING_FOUND;
		}

		if(throwable instanceof SocketTimeoutException) {
			return Reason.TIMEOUT;
		}

		if(throwable instanceof SocketException ||
				throwable instanceof HttpException ||
				throwable instanceof SSLHandshakeException) {
			return Reason.BAD_NETWORK;
		}

		if(throwable instanceof UnimplementedException ||
				throwable instanceof UnsupportedOperationException) {
			return Reason.UNIMPLEMENTED;
		}

		if(throwable instanceof InvalidSyntaxException) {

		}
		 */

		protected abstract boolean isMeImpl(Throwable t);
	}
}