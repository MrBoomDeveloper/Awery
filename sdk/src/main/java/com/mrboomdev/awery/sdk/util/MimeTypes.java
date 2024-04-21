package com.mrboomdev.awery.sdk.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum MimeTypes {
	ANY("*/*", null),
	ZIP("application/zip", "zip"),
	RAR("application/x-rar-compressed", "rar"),
	JAR("application/java-archive", "jar"),
	APK("application/vnd.android.package-archive", "apk"),
	JSON("application/json; charset=UTF-8", "json"),
	HTML("text/html; charset=UTF-8", "html"),
	XML("text/xml; charset=UTF-8", "xml"),
	TEXT("text/plain; charset=UTF-8", "txt"),
	JS("text/javascript", "js");

	private final String extension;
	private final String text;

	MimeTypes(String text, String extension) {
		this.extension = extension;
		this.text = text;
	}

	@Contract(pure = true)
	public static boolean test(String fileName, @NotNull MimeTypes... possibleTypes) {
		for(var type : possibleTypes) {
			if(type == ANY || fileName.endsWith("." + type.extension)) {
				return true;
			}
		}

		return false;
	}

	@NotNull
	@Override
	public String toString() {
		return text;
	}
}