package com.mrboomdev.awery.util.ui.dialog;

import android.content.Context;
import android.view.View;

public class DialogColorPickerField implements DialogField {
	private View view;

	@Override
	public View getView() {
		return view;
	}

	@Override
	public void deAttach() {
		this.view = null;
	}
}