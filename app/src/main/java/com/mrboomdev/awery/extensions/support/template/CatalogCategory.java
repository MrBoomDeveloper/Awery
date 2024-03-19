package com.mrboomdev.awery.extensions.support.template;

import androidx.annotation.NonNull;

import java.util.List;

public abstract class CatalogCategory {
	public static final Throwable NOT_IMPLEMENTED = new UnsupportedOperationException("Not implemented!");

	public void load(int page, @NonNull OnLoadListener listener) {
		listener.onFailed(NOT_IMPLEMENTED);
	}

	public void load(OnLoadListener listener) {
		load(0, listener);
	}

	public abstract String getTitle();

	public String getDescription() {
		return null;
	}

	public interface OnLoadListener {
		void onLoaded(List<CatalogMedia> media);

		void onFailed(Throwable t);
	}
}