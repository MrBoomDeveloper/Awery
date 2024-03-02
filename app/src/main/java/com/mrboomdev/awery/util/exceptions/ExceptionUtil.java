package com.mrboomdev.awery.util.exceptions;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import eu.kanade.tachiyomi.network.HttpException;
import kotlinx.serialization.json.internal.JsonDecodingException;

public class ExceptionUtil {
	public static final UnimplementedException NOT_IMPLEMENTED = new UnimplementedException("Not implemented!");
	public static final IllegalStateException ZERO_RESULTS = new IllegalStateException("Zero results were found!");
	private final Throwable t;

	public ExceptionUtil(Throwable t) {
		this.t = t;
	}

	public boolean isGenericError() {
		return t == NOT_IMPLEMENTED || t == ZERO_RESULTS;
	}

	public String getTitle(Context context) {
		if(t == ZERO_RESULTS) {
			return "Nothing found!";
		} else if(t == NOT_IMPLEMENTED) {
			return "Feature not implemented!";
		} else if(t instanceof SocketTimeoutException) {
			return "Connection timed out!";
		} else if(t instanceof SocketException || t instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(t instanceof HttpException e) {
			return switch(e.getCode()) {
				case 403 -> "Access denied!";
				case 404 -> "Nothing found!";
				case 429 -> "Too much requests!";
				case 500, 503 -> "Server is down!";
				case 504 -> "Connection timed out!";
				default -> getGenericTitle(context);
			};
		} else if(t instanceof UnsupportedOperationException) {
			return "Feature not implemented!";
		} else if(t instanceof JsonDecodingException) {
			return "Parser is broken!";
		} else if(t instanceof UnknownHostException) {
			return "No internet connection!";
		}

		return getGenericTitle(context);
	}

	public boolean isProgramException() {
		return !(t == ZERO_RESULTS ||
				t == NOT_IMPLEMENTED ||
				t instanceof SocketTimeoutException ||
				t instanceof HttpException ||
				t instanceof SSLHandshakeException);
	}

	@NonNull
	@Contract(pure = true)
	private String getGenericTitle(Context context) {
		return "Something just went wrong!";
	}

	@NonNull
	private String getGenericMessage() {
		return Log.getStackTraceString(t);
	}

	public String getMessage(Context context) {
		if(t instanceof SocketTimeoutException) {
			return "The connection timed out, please try again later.";
		} else if(t instanceof SocketException || t instanceof SSLHandshakeException) {
			return "Failed to connect to the server!";
		} else if(t instanceof HttpException e) {
			return switch(e.getCode()) {
				case 403 -> "You have no access to this resource, try logging into your account.";
				case 404 -> "The resource you requested does not exist!";
				case 429 -> "You have exceeded the rate limit, please try again later.";
				case 500 -> "An internal server error has occurred, please try again later.";
				case 503 -> "The service is temporarily unavailable, please try again later.";
				case 504 -> "The connection timed out, please try again later.";
				default -> getGenericMessage();
			};
		} else if(t instanceof UnknownHostException e) {
			return e.getMessage();
		}

		return getGenericMessage();
	}
}