package com.mrboomdev.awery.util.ui.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

public final class DialogBuilder extends BaseDialogBuilder<DialogBuilder> {

	public DialogBuilder(Context context) {
		super(context);
	}

	@Nullable
	@Override
	protected View getContentView(View parentView) {
		return null;
	}
}