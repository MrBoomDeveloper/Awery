package com.mrboomdev.awery.anilist.search;

import androidx.annotation.NonNull;

public enum SearchSort {
	TRENDING_ASCENDING("TRENDING"),
	TRENDING_DESCENDING("TRENDING_DESC");

	private final String text;

	SearchSort(String text) {
		this.text = text;
	}


	@NonNull
	@Override
	public String toString() {
		return text;
	}
}