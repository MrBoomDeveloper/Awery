package com.mrboomdev.awery.extensions;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.List;

public class Extension implements Comparable<Extension> {
	public static final int FLAG_VIDEO_EXTENSION = 1;
	public static final int FLAG_BOOK_EXTENSION = 2;
	public static final int FLAG_SUBTITLES_EXTENSION = 4;
	public static final int FLAG_TRACKER_EXTENSION = 8;
	public static final int FLAG_ERROR = 16;
	public static final int FLAG_WORKING = 32;
	public static final int FLAG_NSFW = 64;
	private final String version, id;
	private final String name;
	private boolean isLoaded;
	private int flags;
	private String error;
	private Throwable exception;
	@Json(ignore = true)
	private final List<ExtensionProvider> providers = new ArrayList<>();
	@Json(ignore = true)
	private final ExtensionsManager manager;

	public Extension(ExtensionsManager manager, String id, String name, String version) {
		this.name = name;
		this.version = version;
		this.id = id;
		this.manager = manager;
		addFlags(FLAG_WORKING);
	}

	public ExtensionsManager getManager() {
		return manager;
	}

	public int getFlags() {
		return flags;
	}

	public void clearProviders() {
		providers.clear();
	}

	public void setIsLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public void addFlags(int flags) {
		this.flags |= flags;
	}

	public void removeFlags(int flags) {
		this.flags &= ~flags;
	}

	public Drawable getIcon() {
		return null;
	}

	public List<ExtensionProvider> getProviders() {
		return providers;
	}

	public void addProvider(ExtensionProvider provider) {
		this.providers.add(provider);
	}

	public String getErrorTitle() {
		return error;
	}

	public String getId() {
		return id;
	}

	public boolean isVideoExtension() {
		return (flags & FLAG_VIDEO_EXTENSION) == FLAG_VIDEO_EXTENSION;
	}

	public boolean isBookExtension() {
		return (flags & FLAG_BOOK_EXTENSION) == FLAG_BOOK_EXTENSION;
	}

	public boolean isTrackerExtension() {
		return (flags & FLAG_TRACKER_EXTENSION) == FLAG_TRACKER_EXTENSION;
	}

	public boolean isSubtitlesExtension() {
		return (flags & FLAG_SUBTITLES_EXTENSION) == FLAG_SUBTITLES_EXTENSION;
	}

	public void setError(String error, Throwable e) {
		if(error == null && e == null) {
			removeFlags(FLAG_ERROR);
			addFlags(FLAG_WORKING);

			this.error = null;
			this.exception = null;
			return;
		}

		this.error = error;
		this.exception = e;

		addFlags(FLAG_ERROR);
		removeFlags(FLAG_WORKING);
	}

	public Throwable getError() {
		return exception;
	}

	public void setError(String error) {
		setError(error, null);
	}

	public void setError(Throwable e) {
		setError(e.getMessage(), e);
	}

	public void removeError() {
		setError(null, null);
	}

	public boolean isError() {
		return (flags & FLAG_ERROR) == FLAG_ERROR;
	}

	public String getName() {
		return name;
	}

	public boolean isNsfw() {
		return (flags & FLAG_NSFW) == FLAG_NSFW;
	}

	public String getVersion() {
		return version;
	}

	@NonNull
	@Override
	public String toString() {
		var builder = new StringBuilder("{ \"");
		builder.append(id);
		builder.append("\": \"");
		builder.append(name);
		builder.append("\", ");
		builder.append(version);
		builder.append("\"");

		if(exception != null) {
			builder.append(", \"exception\": \"");
			builder.append(exception.getLocalizedMessage());
			builder.append("\"");
		}

		builder.append(" }");
		return builder.toString();
	}

	@Override
	public int compareTo(@NonNull Extension o) {
		return name.compareToIgnoreCase(o.name);
	}
}