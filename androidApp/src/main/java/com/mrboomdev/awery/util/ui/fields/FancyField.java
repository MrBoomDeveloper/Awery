package com.mrboomdev.awery.util.ui.fields;

import android.view.View;

public abstract class FancyField<T extends View> {
	private T view;

	public T getView() {
		return getView(true);
	}

	public T getView(boolean createIfDoesNotExist) {
		if(!createIfDoesNotExist) return view;
		return view != null ? view : (view = createView());
	}

	public boolean isCreated() {
		return view != null;
	}

	public abstract T createView();
}