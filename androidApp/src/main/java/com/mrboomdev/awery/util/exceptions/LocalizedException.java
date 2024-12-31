package com.mrboomdev.awery.util.exceptions;

import android.content.Context;

public interface LocalizedException {
	default String getDescription(Context context) {
		return getLocalizedMessage();
	}

	String getTitle(Context context);

	String getLocalizedMessage();
}