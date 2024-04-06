package com.mrboomdev.awery.util.exceptions;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.mrboomdev.awery.R;

/**
 * Being thrown if no results was found.
 * @author MrBoomDev
 */
public class ZeroResultsException extends RuntimeException implements LocalizedException {
	private final int description;

	public ZeroResultsException(String message, @StringRes int localizedDescription) {
		super(message);
		this.description = localizedDescription;
	}

	@Override
	public String getDescription(@NonNull Context context) {
		return context.getString(description);
	}

	@Override
	public String getTitle(@NonNull Context context) {
		return context.getString(R.string.nothing_found);
	}
}