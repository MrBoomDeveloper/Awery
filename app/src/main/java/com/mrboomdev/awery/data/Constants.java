package com.mrboomdev.awery.data;

public class Constants {
	public static final String SUPPRESS_IGNORED_THROWABLE = "ThrowableResultOfMethodCallIgnored";
	public static final String DEFAULT_UA = "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36";
	public static final String DIRECTORY_NET_CACHE = "network_cache";
	public static final String DIRECTORY_IMAGE_CACHE = "img";
	public static final String DIRECTORY_WEBVIEW_CACHE = "WebView";

	/**
	 * Typically your IDE will warn if you have any code after a return statement,
	 * but this value will let you keep the code uncommented!
	 */
	public static boolean alwaysTrue() {
		return true;
	}

	/**
	 * Typically your IDE will warn if you have any code after a return statement,
	 * but this value will let you keep the code uncommented!
	 */
	public static boolean alwaysFalse() {
		return false;
	}

	/**
	 * Typically your IDE will warn if you have any code after a throw statement,
	 * but this method will let you keep the code uncommented!
	 */
	public static void alwaysThrowException() {
		throw new RuntimeException();
	}
}