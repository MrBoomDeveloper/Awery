package com.mrboomdev.awery.util.exceptions;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.util.graphql.GraphQLException;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import eu.kanade.tachiyomi.network.HttpException;
import kotlinx.serialization.json.internal.JsonDecodingException;

public class ExceptionDescriptor {
	public static final Adapter ADAPTER = new Adapter();
	private final Throwable throwable;

	public ExceptionDescriptor(@NonNull Throwable t) {
		if(!isUnknownException(t)) {
			throwable = t;
			return;
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
					this.throwable = cause;
					return;
				}
			}

			this.throwable = causes.get(causes.size() - 1);
		} else {
			this.throwable = t;
		}
	}

	public String getTitle(Context context) {
		if(throwable instanceof ZeroResultsException) {
			return "Nothing found!";
		} else if(throwable instanceof UnimplementedException) {
			return "Feature not implemented!";
		} else if(throwable instanceof SocketTimeoutException) {
			return "Connection timed out!";
		} else if(throwable instanceof SocketException e) {
			return e.getMessage();
		} else if(throwable instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(throwable instanceof HttpException e) {
			return switch(e.getCode()) {
				case 403 -> "Access denied!";
				case 404 -> "Nothing found!";
				case 429 -> "Too much requests!";
				case 500, 503 -> "Server is down!";
				case 504 -> "Connection timed out!";
				default -> getGenericTitle(context);
			};
		} else if(throwable instanceof UnsupportedOperationException) {
			return "Feature not implemented!";
		} else if(throwable instanceof JsonDecodingException | throwable instanceof NullPointerException) {
			return "Parser is broken!";
		} else if(throwable instanceof UnknownHostException) {
			return "No internet connection!";
		} else if(throwable instanceof GraphQLException) {
			return "Failed to query the server!";
		}

		return getGenericTitle(context);
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
				t instanceof SocketException ||
				t instanceof SSLHandshakeException ||
				t instanceof HttpException ||
				t instanceof UnsupportedOperationException ||
				t instanceof JsonDecodingException ||
				t instanceof NullPointerException ||
				t instanceof UnknownHostException ||
				t instanceof GraphQLException);
	}

	@NonNull
	@Contract(pure = true)
	private String getGenericTitle(Context context) {
		return "Something just went wrong!";
	}

	@NonNull
	private static String getGenericMessage(Throwable throwable) {
		return Log.getStackTraceString(throwable);
	}

	public String getShortDescription() {
		return throwable.getMessage();
	}

	public String getMessage(Context context) {
		if(throwable.getCause() != null) {
			return new ExceptionDescriptor(throwable.getCause()).getMessage(context);
		}

		return getMessage(throwable, context);
	}

	private static String getMessage(@NonNull Throwable throwable, Context context) {
		if(throwable instanceof ZeroResultsException) {
			return throwable.getMessage();
		} else if(throwable instanceof UnimplementedException) {
			return throwable.getMessage();
		} else if(throwable instanceof SocketTimeoutException) {
			return "The connection timed out, please try again later.";
		} else if(throwable instanceof SocketException || throwable instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(throwable instanceof HttpException e) {
			return switch(e.getCode()) {
				case 400 -> "Error 400. The request was invalid, please try again later.";
				case 401 -> "Error 401. You are not logged in, please log in and try again.";
				case 403 -> "Error 403. You have no access to this resource, try logging into your account.";
				case 404 -> "Error 404. The resource you requested does not exist!";
				case 429 -> "Error 429. You have exceeded the rate limit, please try again later.";
				case 500 -> "Error 500. An internal server error has occurred, please try again later.";
				case 503 -> "Error 503. The service is temporarily unavailable, please try again later.";
				case 504 -> "Error 504. The connection timed out, please try again later.";
				default -> getGenericMessage(throwable);
			};
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
		}

		return getGenericMessage(throwable);
	}

	@NonNull
	@Override
	public String toString() {
		return Log.getStackTraceString(throwable);
	}

	public static class Adapter {

		@ToJson
		public String toJson(Throwable serializable) throws IOException {
			try(var arrayStream = new ByteArrayOutputStream(); var outputStream = new ObjectOutputStream(arrayStream)) {
				outputStream.writeObject(serializable);
				return Base64.encodeToString(arrayStream.toByteArray(), Base64.DEFAULT);
			}
		}

		@FromJson
		public Throwable fromJson(@NonNull String string) throws IOException, ClassNotFoundException {
			var data = Base64.decode(string, Base64.DEFAULT);

			try(var stream = new ObjectInputStream(new ByteArrayInputStream(data))) {
				return (Throwable) stream.readObject();
			}
		}
	}
}