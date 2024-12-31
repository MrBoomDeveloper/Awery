package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyActivity;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

public final class DialogBuilder extends BaseDialogBuilder<DialogBuilder> {

	public DialogBuilder(@NonNull Context context) {
		super(context);
	}

	public DialogBuilder() {
		this(Objects.requireNonNull(getAnyActivity(AppCompatActivity.class)));
	}

	@Contract(pure = true)
	@Nullable
	@Override
	protected View getContentView(View parentView) {
		return null;
	}
}