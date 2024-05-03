package com.mrboomdev.awery.util.exceptions;

import android.content.Context;
import android.os.Build;
import android.os.strictmode.InstanceCountViolation;
import android.os.strictmode.Violation;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.sdk.util.exceptions.InvalidSyntaxException;
import com.mrboomdev.awery.util.graphql.GraphQLException;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.WrappedException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import eu.kanade.tachiyomi.network.HttpException;
import java9.util.Objects;
import kotlinx.serialization.json.internal.JsonDecodingException;

public class ExceptionDescriptor {
	private final Throwable throwable;

	public ExceptionDescriptor(@NonNull Throwable t) {
		this.throwable = unwrap(t);
	}

	public static Throwable unwrap(Throwable t) {
		if(t instanceof WrappedException wrappedException) {
			return unwrap(wrappedException.getWrappedException());
		}

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

	public String getTitle(Context context) {
		if(throwable instanceof LocalizedException e) {
			return e.getTitle(context);
		} else if(throwable instanceof UnimplementedException
				|| throwable instanceof UnsupportedOperationException) {
			return context.getString(R.string.not_implemented);
		} else if(throwable instanceof SocketTimeoutException) {
			return context.getString(R.string.timed_out);
		} else if(throwable instanceof SocketException e) {
			return e.getMessage();
		} else if(throwable instanceof SSLHandshakeException) {
			return context.getString(R.string.failed_handshake);
		} else if(throwable instanceof HttpException e) {
			return switch(e.getCode()) {
				case 403 -> context.getString(R.string.access_denied);
				case 404 -> context.getString(R.string.nothing_found);
				case 429 -> context.getString(R.string.too_much_requests);
				case 500, 503 -> context.getString(R.string.server_down);
				case 504 -> context.getString(R.string.timed_out);
				default -> getGenericTitle(context);
			};
		} else if(throwable instanceof GraphQLException
				| throwable instanceof NullPointerException) {
			return "Parser is broken!";
		} else if(throwable instanceof UnknownHostException) {
			return context.getString(R.string.no_internet);
		} else {
			if(throwable instanceof InvalidSyntaxException
					|| throwable instanceof JsonDecodingException) {
				return "Parser has crashed!";
			} else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && throwable instanceof Violation) {
				return "Bad thing has happened...";
			}
		}

		return getGenericTitle(context);
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
		if(throwable instanceof JsException js) {
			return switch(Objects.requireNonNullElse(js.getErrorId(), "")) {
				case JsException.ERROR_RATE_LIMITED,
						JsException.ERROR_HTTP,
						JsException.SERVER_ERROR,
						JsException.SERVER_DOWN -> true;
				default -> false;
			};
		}

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
				t instanceof SocketException ||
				t instanceof InvalidSyntaxException ||
				t instanceof JsException ||
				t instanceof SSLHandshakeException ||
				t instanceof HttpException ||
				t instanceof UnsupportedOperationException ||
				t instanceof JsonDecodingException ||
				t instanceof UnknownHostException ||
				t instanceof GraphQLException);
	}

	@NonNull
	@Contract(pure = true)
	private String getGenericTitle(@NonNull Context context) {
		return context.getString(R.string.something_went_wrong);
	}

	@NonNull
	private static String getGenericMessage(Throwable throwable) {
		return Log.getStackTraceString(throwable);
	}

	public String getMessage(Context context) {
		return getMessage(throwable, context);
	}

	private static String getMessage(@NonNull Throwable throwable, Context context) {
		if(throwable instanceof LocalizedException e) {
			return e.getDescription(context);
		} else if(throwable instanceof UnimplementedException) {
			return throwable.getMessage();
		} else if(throwable instanceof SocketTimeoutException) {
			return "The connection timed out, please try again later.";
		} else if(throwable instanceof SocketException || throwable instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(throwable instanceof HttpException e) {
			return getHttpErrorTitle(context, e.getCode());
		} else if(throwable instanceof UnknownHostException e) {
			return e.getMessage();
		} else if(throwable instanceof GraphQLException e) {
			var errors = e.getGraphQLErrors();
			var builder = new StringBuilder();

			var iterator = errors.iterator();
			while(iterator.hasNext()) {
				var error = iterator.next();
				builder.append(error.toUserReadableString());

				if(iterator.hasNext()) {
					builder.append("\n");
				}
			}

			return builder.toString();
		} if(throwable instanceof JsonDecodingException ||
				throwable instanceof InvalidSyntaxException) {
			return "An error has occurred while parsing the response. " + throwable.getMessage();
		} else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && throwable instanceof Violation) {
			if(throwable instanceof InstanceCountViolation) {
				return "Too much instances of the object was created. " + throwable.getMessage();
			}

			return "Bad thing has happened...";
		}

		return getGenericMessage(throwable);
	}

	private static String getHttpErrorTitle(Context context, int code) {
		return switch(code) {
			case 400 -> "Error 400. The request was invalid, please try again later.";
			case 401 -> "Error 401. You are not logged in, please log in and try again.";
			case 403 -> "Error 403. You have no access to this resource, try logging into your account.";
			case 404 -> context.getString(R.string.not_found_detailed);
			case 429 -> "Error 429. You have exceeded the rate limit, please try again later.";
			case 500 -> "Error 500. An internal server error has occurred, please try again later.";
			case 503 -> "Error 503. The service is temporarily unavailable, please try again later.";
			case 504 -> "Error 504. The connection timed out, please try again later.";
			default -> "Error " + code + ". An unknown error has occurred, please try again later.";
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
				if(t instanceof JsException js) {
					return switch(Objects.requireNonNullElse(js.getErrorId(), "")) {
						case JsException.SERVER_DOWN, JsException.SERVER_ERROR -> true;
						default -> false;
					};
				}

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
				if(t instanceof UnsupportedOperationException || t instanceof UnimplementedException) {
					return true;
				}

				return false;
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