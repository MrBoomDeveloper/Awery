package com.mrboomdev.awery.util.ui.dialog;

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

	public void show() {
		dismiss();

		dialog = isLandscape() ? new SideSheetDialog(context) {
			@Override
			protected void onStart() {
				super.onStart();
				DialogUtils.fixDialog(dialog);
			}
		} : new BottomSheetDialog(context) {
			@Override
			protected void onStart() {
				super.onStart();
				DialogUtils.fixDialog(dialog);
			}
		};

		dialog.setContentView(getContentView(context));
		dialog.show();
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