package com.mrboomdev.awery.app.data;

import android.os.Build;

import java.util.List;

public class Constants {
	public static final String CATALOG_LIST_BLACKLIST = "7";
	public static final String CATALOG_LIST_HISTORY = "9";
	public static final String CATALOG_LIST_TRACKED = "CATALOG_LIST_TRACKED";

	public static final List<String> HIDDEN_LISTS = List.of(
			CATALOG_LIST_BLACKLIST, CATALOG_LIST_HISTORY, CATALOG_LIST_TRACKED);

	public static final String DEFAULT_UA = "Mozilla/5.0 (Linux; Android "
			+ Build.VERSION.RELEASE
			+ "; "
			+ "Pixel 6"
			+ ") AppleWebKit/"
			+ "537.36"
			+ "(KHTML, like Gecko) Chrome/"
			+ "112.0.0.0"
			+ "Mobile Safari/"
			+ "537.36";

	public static final String DIRECTORY_NET_CACHE = "network_cache";
	public static final String DIRECTORY_IMAGE_CACHE = "img";
	public static final String DIRECTORY_WEBVIEW_CACHE = "WebView";

	/**
	 * Should be inside of {@link #DIRECTORY_NET_CACHE}
	 */
	public static final String FILE_FEEDS_NET_CACHE = "feeds.json";

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

	public static <T> T returnMe(T t) {
		return t;
	}

	/**
	 * Typically your IDE will warn if you have any code after a throw statement,
	 * but this method will let you keep the code uncommented!
	 */
	public static void alwaysThrowException() {
		if(alwaysTrue()) throw new RuntimeException();
	}
}