package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;

import android.content.Context;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class WeakContext {
	private final boolean useAnyContext;
	private WeakReference<Context> theContext;

	public WeakContext(Context context) {
		theContext = new WeakReference<>(context);
		useAnyContext = false;
	}

	/**
	 * You can get a new {@link Context} by using this constructor after every call to {@link #get()}.
	 * @author MrBoomDev
	 */
	public WeakContext() {
		useAnyContext = true;
	}

	public Context get() {
		var existing = getExisting();
		if(existing != null) return existing;

		if(useAnyContext) {
			var any = getAnyContext();
			theContext = new WeakReference<>(any);
			return any;
		}

		return null;
	}

	@Nullable
	private Context getExisting() {
		if(theContext == null) {
			return null;
		}

		return theContext.get();
	}
}