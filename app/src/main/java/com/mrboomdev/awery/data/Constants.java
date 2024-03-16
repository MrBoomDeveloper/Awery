package com.mrboomdev.awery.data;

public class Constants {
	public static final String DEFAULT_UA = "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36";

	public static boolean alwaysTrue() {
		return true;
	}

	public static boolean alwaysFalse() {
		return false;
	}

	public static void alwaysThrowException() {
		throw new RuntimeException();
	}
}