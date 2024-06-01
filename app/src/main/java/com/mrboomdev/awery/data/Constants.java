package com.mrboomdev.awery.data;

import android.os.Build;
import android.webkit.WebView;

import com.mrboomdev.awery.extensions.support.js.JsManager;

import java.util.List;

public class Constants {
	public static final String CATALOG_LIST_BLACKLIST = "7";
	public static final String CATALOG_LIST_HISTORY = "9";
	public static final List<String> HIDDEN_LISTS = List.of(CATALOG_LIST_BLACKLIST, CATALOG_LIST_HISTORY);
	public static final String LOGS_SEPARATOR = "-".repeat(75);
	public static final String SUPPRESS_IGNORED_THROWABLE = "ThrowableResultOfMethodCallIgnored";

	//TODO: Remove these fields after JS extensions will be made
	public static final String ANILIST_EXTENSION_ID = "com.mrboomdev.awery.extension.anilist";
	public static final String ANILIST_CATALOG_ITEM_ID_PREFIX =
			new JsManager().getId() + ";;;" + ANILIST_EXTENSION_ID + ";;;";

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