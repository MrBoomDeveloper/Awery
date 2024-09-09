package com.mrboomdev.awery.util.exceptions;

import static com.mrboomdev.awery.app.App.i18n;

import androidx.annotation.StringRes;

import com.mrboomdev.awery.R;

/**
 * Being thrown if no results was found.
 * @author MrBoomDev
 */
public class ZeroResultsException extends RuntimeException implements LocalizedException {
	private final int description, title;

	public ZeroResultsException(String message, @StringRes int localizedTitle, @StringRes int localizedDescription) {
		super(message);
		this.title = localizedTitle;
		this.description = localizedDescription;
	}

	public ZeroResultsException(String message, @StringRes int localizedDescription) {
		super(message);
		this.title = localizedDescription;
		this.description = localizedDescription;
	}

	public ZeroResultsException(String message) {
		super(message);
		this.title = -1;
		this.description = -1;
	}

	@Override
	public String getLocalizedMessage() {
		if(description == -1) {
			return getLocalizedMessage();
		}

		return i18n(description);
	}

	@Override
	public String getTitle() {
		if(title == -1) {
			return getLocalizedMessage();
		}

		return i18n(R.string.nothing_found);
	}
}