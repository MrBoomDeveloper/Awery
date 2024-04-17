package com.mrboomdev.awery.extensions.data;

import java.util.List;

public class CatalogTrackingOptions {
	public static final int FEATURE_PRIVATE = 1;
	public static final int FEATURE_LISTS = 2;
	public static final int FEATURE_LISTS_CREATE = 4;
	public static final int FEATURE_LISTS_DELETE = 8;
	public static final int FEATURE_DATE_START = 16;
	public static final int FEATURE_DATE_END = 32;
	public static final int FEATURE_PROGRESS = 64;
	public static final int FEATURE_SCORE = 128;
	private final int flags;
	public List<String> lists;
	public String currentList;
	public long fromDate, toDate;
	public boolean isPrivate;
	public float progress, score;

	public CatalogTrackingOptions(int flags) {
		this.flags = flags;
	}

	public boolean hasFeatures(int flags) {
		return (this.flags & flags) == flags;
	}
}