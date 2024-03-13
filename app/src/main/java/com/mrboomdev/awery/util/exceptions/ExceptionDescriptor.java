package com.mrboomdev.awery.util.exceptions;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

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

import javax.net.ssl.SSLHandshakeException;

import eu.kanade.tachiyomi.network.HttpException;
import kotlinx.serialization.json.internal.JsonDecodingException;

public class ExceptionDescriptor {
	public static final Adapter ADAPTER = new Adapter();
	private final Throwable exception;

	public ExceptionDescriptor(Throwable t) {
		this.exception = t;
	}

	public boolean isGenericError() {
		return exception instanceof UnimplementedException || exception instanceof ZeroResultsException;
	}

	public String getTitle(Context context) {
		if(exception instanceof ZeroResultsException) {
			return "Nothing found!";
		} else if(exception instanceof UnimplementedException) {
			return "Feature not implemented!";
		} else if(exception instanceof SocketTimeoutException) {
			return "Connection timed out!";
		} else if(exception instanceof SocketException || exception instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(exception instanceof HttpException e) {
			return switch(e.getCode()) {
				case 403 -> "Access denied!";
				case 404 -> "Nothing found!";
				case 429 -> "Too much requests!";
				case 500, 503 -> "Server is down!";
				case 504 -> "Connection timed out!";
				default -> getGenericTitle(context);
			};
		} else if(exception instanceof UnsupportedOperationException) {
			return "Feature not implemented!";
		} else if(exception instanceof JsonDecodingException | exception instanceof NullPointerException) {
			return "Parser is broken!";
		} else if(exception instanceof UnknownHostException) {
			return "No internet connection!";
		}

		return getGenericTitle(context);
	}

	public boolean isProgramException() {
		return !(exception instanceof ZeroResultsException ||
				exception instanceof UnimplementedException ||
				exception instanceof SocketTimeoutException ||
				exception instanceof HttpException ||
				exception instanceof SSLHandshakeException);
	}

	@NonNull
	@Contract(pure = true)
	private String getGenericTitle(Context context) {
		return "Something just went wrong!";
	}

	@NonNull
	private String getGenericMessage() {
		return Log.getStackTraceString(exception);
	}

	public String getShortDescription() {
		return exception.getMessage();
	}

	public String getMessage(Context context) {
		if(exception instanceof ZeroResultsException) {
			return exception.getMessage();
		} else if(exception instanceof UnimplementedException) {
			return exception.getMessage();
		} else if(exception instanceof SocketTimeoutException) {
			return "The connection timed out, please try again later.";
		} else if(exception instanceof SocketException || exception instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(exception instanceof HttpException e) {
			return switch(e.getCode()) {
				case 400 -> "Error 400. The request was invalid, please try again later.";
				case 401 -> "Error 401. You are not logged in, please log in and try again.";
				case 403 -> "Error 403. You have no access to this resource, try logging into your account.";
				case 404 -> "Error 404. The resource you requested does not exist!";
				case 429 -> "Error 429. You have exceeded the rate limit, please try again later.";
				case 500 -> "Error 500. An internal server error has occurred, please try again later.";
				case 503 -> "Error 503. The service is temporarily unavailable, please try again later.";
				case 504 -> "Error 504. The connection timed out, please try again later.";
				default -> getGenericMessage();
			};
		} else if(exception instanceof UnknownHostException e) {
			return e.getMessage();
		}

		return getGenericMessage();
	}

	@NonNull
	@Override
	public String toString() {
		return Log.getStackTraceString(exception);
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