package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.AweryApp.fixDialog;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AppCompatDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.sidesheet.SideSheetDialog;

public abstract class SheetDialog implements DialogInterface {
	private final Context context;
	private AppCompatDialog dialog;

	public SheetDialog(Context context) {
		this.context = context;
	}

	public abstract View getContentView(Context context);

	public AppCompatDialog getDialog() {
		return dialog;
	}

	public Context getContext() {
		return context;
	}

	public void show() {
		dismiss();

		dialog = isLandscape() ? new SideSheetDialog(context) {
			@Override
			protected void onStart() {
				super.onStart();
				fixDialog(dialog);
			}

			@Override
			protected void onStop() {
				super.onStop();
				dialog = null;
			}

			@Override
			public void onDetachedFromWindow() {
				super.onDetachedFromWindow();
				dialog = null;
			}
		} : new BottomSheetDialog(context) {
			@Override
			protected void onStart() {
				super.onStart();
				fixDialog(dialog);
			}

			@Override
			protected void onStop() {
				super.onStop();
				dialog = null;
			}

			@Override
			public void onDetachedFromWindow() {
				super.onDetachedFromWindow();
				dialog = null;
			}
		};

		dialog.setContentView(getContentView(context));
		dialog.show();
	}

	public boolean isShown() {
		return dialog != null;
	}

	public void dismiss() {
		if(dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	@Override
	public void cancel() {
		if(dialog != null) {
			dialog.cancel();
			dialog = null;
		}
	}
}