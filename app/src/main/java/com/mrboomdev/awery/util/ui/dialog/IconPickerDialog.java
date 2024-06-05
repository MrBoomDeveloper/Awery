package com.mrboomdev.awery.util.ui.dialog;

import android.content.Context;

import com.mrboomdev.awery.util.IconStateful;
import com.mrboomdev.awery.util.Selection;

import java.util.List;

public final class IconPickerDialog extends BaseDialogBuilder<IconPickerDialog> {
	private List<IconStateful> items;

	public IconPickerDialog(Context context) {
		super(context);
	}

	public IconPickerDialog setItems(List<IconStateful> items) {
		this.items = items;
		return this;
	}
}