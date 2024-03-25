package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import okhttp3.MediaType;

public enum MimeTypes {
	ANY("*/*", null),
	ZIP("application/zip", "zip"),
	JAR("application/java-archive", "jar"),
	APK("application/vnd.android.package-archive", "apk"),
	JSON("application/json; charset=UTF-8", "json"),
	JS("text/javascript", "js");

	private final String extension;
	private final String text;

	MimeTypes(String text, String extension) {
		this.extension = extension;
		this.text = text;
	}

	@Contract(pure = true)
	public static boolean test(String fileName, @NonNull MimeTypes... possibleTypes) {
		for(var type : possibleTypes) {
			if(type.extension != null && fileName.endsWith("." +type.extension)) {
				return true;
			}
		}

		return false;
	}

	public MediaType toMediaType() {
		return MediaType.parse(text);
	}

	@NonNull
	@Override
	public String toString() {
		return text;
	}
}