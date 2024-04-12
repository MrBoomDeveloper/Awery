package com.mrboomdev.awery.util.ui.dialog;

import android.view.View;

public class DialogCustomField implements DialogField {
	private View view;

	public DialogCustomField(View view) {
		this.view = view;
	}

	@Override
	public View getView() {
		return view;
	}

	@Override
	public void deAttach() {
		this.view = null;
	}
}