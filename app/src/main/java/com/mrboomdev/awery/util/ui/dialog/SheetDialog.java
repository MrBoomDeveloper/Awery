package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.App.fixDialog;
import static com.mrboomdev.awery.app.App.isLandscape;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AppCompatDialog;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.sidesheet.SideSheetDialog;

/**
 * An adaptive dialog which will stick to the side at landscape orientation
 * and stay at the center if current orientation is portrait.
 * @author MrBoomDev
 */
public abstract class SheetDialog implements DialogInterface {
	private final Context context;
	private AppCompatDialog dialog;
	private OnDismissListener dismissListener;
	private boolean cancelOnClickOutside, cancellable;

	public SheetDialog(Context context) {
		this.context = context;
	}

	public abstract View getContentView(Context context);

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

		setCancelable(cancellable);
		setCanceledOnTouchOutside(cancelOnClickOutside);
		setOnDismissListener(dismissListener);

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

	public void setOnDismissListener(OnDismissListener listener) {
		this.dismissListener = listener;

		if(dialog != null) {
			dialog.setOnDismissListener(listener);
		}
	}

	public void setCanceledOnTouchOutside(boolean cancel) {
		this.cancelOnClickOutside = cancel;

		if(dialog != null) {
			dialog.setCanceledOnTouchOutside(cancel);
		}
	}

	public void setCancelable(boolean cancel) {
		this.cancellable = cancel;

		if(dialog != null) {
			dialog.setCancelable(cancel);
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