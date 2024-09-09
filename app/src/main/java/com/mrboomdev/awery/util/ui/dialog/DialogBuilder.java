package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.Lifecycle.getAnyActivity;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public final class DialogBuilder extends BaseDialogBuilder<DialogBuilder> {

	public DialogBuilder(Context context) {
		super(context);
	}

	public DialogBuilder() {
		this(getAnyActivity(AppCompatActivity.class));
	}

	@Nullable
	@Override
	protected View getContentView(View parentView) {
		return null;
	}
}