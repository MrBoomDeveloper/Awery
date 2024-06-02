package com.mrboomdev.awery.util.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

public class DialogBuilder {
	private final Context context;
	private final List<View> fields = new LinkedList<>();
	private final WeakHashMap<View, Object> addedFields = new WeakHashMap<>();
	private ScrollView scroller;
	private ViewGroup fieldsWrapper;
	private Dialog dialog;
	private Callbacks.Callback1<DialogBuilder> dismissListener;
	private OnButtonClickListener okListener, cancelListener, neutralListener;
	private String title, message, okButtonLabel, cancelButtonLabel, neutralButtonLabel;
	private boolean isCancelable = true, didCreateRoot;

	public DialogBuilder(Context context) {
		this.context = context;
	}

	public DialogBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	public DialogBuilder setTitle(@StringRes int res) {
		return setTitle(context.getString(res));
	}

	public DialogBuilder setMessage(String message) {
		this.message = message;
		return this;
	}

	public DialogBuilder setMessage(@StringRes int res) {
		return setMessage(context.getString(res));
	}

	public DialogBuilder setPositiveButton(String label, OnButtonClickListener listener) {
		this.okListener = listener;
		this.okButtonLabel = label;
		return this;
	}

	public DialogBuilder setPositiveButton(@StringRes int label, OnButtonClickListener listener) {
		return setPositiveButton(context.getString(label), listener);
	}

	public DialogBuilder setNeutralButton(String label, OnButtonClickListener listener) {
		this.neutralListener = listener;
		this.neutralButtonLabel = label;
		return this;
	}

	public DialogBuilder setNeutralButton(@StringRes int label, OnButtonClickListener listener) {
		return setNeutralButton(context.getString(label), listener);
	}

	public DialogBuilder setCancelable(boolean isCancelable) {
		this.isCancelable = isCancelable;
		return this;
	}

	public void performPositiveClick() {
		if(okListener != null) {
			okListener.clicked(this);
		}
	}

	public void performNeutralClick() {
		if(neutralListener != null) {
			neutralListener.clicked(this);
		}
	}

	public void performNegativeClick() {
		if(cancelListener != null) {
			cancelListener.clicked(this);
		}
	}

	public DialogBuilder setNegativeButton(String label, OnButtonClickListener listener) {
		this.cancelListener = listener;
		this.cancelButtonLabel = label;
		return this;
	}

	public DialogBuilder setNegativeButton(@StringRes int label, OnButtonClickListener listener) {
		return setNegativeButton(context.getString(label), listener);
	}

	public DialogBuilder setOnDismissListener(Callbacks.Callback1<DialogBuilder> listener) {
		this.dismissListener = listener;
		return this;
	}

	public DialogBuilder addView(View field, int index) {
		fields.add(index, field);

		if(fieldsWrapper != null) {
			addField(field, index);
		}

		return this;
	}

	private void addField(View field, int index) {
		if(addedFields.get(field) != null) {
			return;
		}

		fieldsWrapper.addView(field, index);
		addedFields.put(field, new Object());
	}

	public DialogBuilder addView(View view) {
		addView(view, fields.size());
		return this;
	}

	public DialogBuilder addView(@NonNull Callbacks.Result1<View, ViewGroup> callback) {
		createRoot();
		addView(callback.run(fieldsWrapper));
		return this;
	}

	private void createRoot() {
		if(didCreateRoot) return;
		didCreateRoot = true;

		/*var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);
		linear.setBackgroundResource(R.drawable.dialog_background);*/

		scroller = new ScrollView(context);
		scroller.setVerticalScrollBarEnabled(false);

		var fieldsLinear = new LinearLayoutCompat(context);
		ViewUtil.setHorizontalPadding(fieldsLinear, ViewUtil.dpPx(24));
		fieldsLinear.setOrientation(LinearLayoutCompat.VERTICAL);
		scroller.addView(fieldsLinear, new ViewGroup.LayoutParams(ViewUtil.MATCH_PARENT, ViewUtil.WRAP_CONTENT));
		fieldsWrapper = fieldsLinear;
	}

	public DialogBuilder create() {
		if(dialog != null) {
			dismiss();
		}

		createRoot();

		if(title != null) {
			/*var text = new AppCompatTextView(context);
			text.setTextSize(ViewUtil.spPx(18));
			text.setText(title);

			ViewUtil.setPadding(text, ViewUtil.dpPx(16));
			linear.addView(text, 0);*/
		}

		if(message == null) {
			ViewUtil.setTopPadding(scroller, ViewUtil.dpPx(8));
		}

		//linear.addView(scroller);

		for(var field : fields) {
			addField(field, -1);
		}

		/*var actionsLinear = new LinearLayoutCompat(context);
		actionsLinear.setOrientation(LinearLayoutCompat.HORIZONTAL);
		linear.addView(actionsLinear);*/

		/*if(cancel != null) {
			var button = new AppCompatTextView(context);
			button.setBackgroundResource(R.drawable.ripple_round_you);
			ViewUtil.setPadding(button, ViewUtil.dpPx(16));
			button.setText(cancel);
			actionsLinear.addView(button);

			button.setOnClickListener(v -> {
				if(cancelListener == null) {
					dialog.dismiss();
					return;
				}

				cancelListener.clicked(this);
			});
		}

		if(ok != null) {
			var button = new AppCompatTextView(context);
			button.setBackgroundResource(R.drawable.ripple_round_you);
			ViewUtil.setPadding(button, ViewUtil.dpPx(16));
			button.setText(ok);
			actionsLinear.addView(button);

			button.setOnClickListener(v -> {
				if(okListener == null) {
					dialog.dismiss();
					return;
				}

				okListener.clicked(this);
			});
		}*/

		var builder = new MaterialAlertDialogBuilder(context);
		builder.setCancelable(isCancelable);

		if(fieldsWrapper.getChildCount() > 0) {
			builder.setView(scroller);
		}

		if(title != null) builder.setTitle(title);
		if(message != null) builder.setMessage(message);

		if(okButtonLabel != null) builder.setPositiveButton(okButtonLabel,
				(dialog, which) -> okListener.clicked(this));

		if(cancelButtonLabel != null) builder.setNegativeButton(cancelButtonLabel,
				(dialog, which) -> cancelListener.clicked(this));

		if(neutralButtonLabel != null) builder.setNeutralButton(neutralButtonLabel,
				(dialog, which) -> neutralListener.clicked(this));

		if(dismissListener != null) builder.setOnDismissListener(
				v -> dismissListener.run(this));

		var alertDialog = builder.show();
		dialog = alertDialog;

		if(okButtonLabel != null) alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
				.setOnClickListener(v -> okListener.clicked(this));

		if(cancelButtonLabel != null) alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
				.setOnClickListener(v -> cancelListener.clicked(this));

		if(neutralButtonLabel != null) alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
				.setOnClickListener(v -> neutralListener.clicked(this));

		/*dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linear);
		getWindow().setBackgroundDrawable(null);*/
		return this;
	}

	public DialogBuilder show() {
		if(dialog == null) {
			create();
		}

		dialog.show();
		return this;
	}

	public boolean isShown() {
		return dialog != null && dialog.isShowing();
	}

	public DialogBuilder dismiss() {
		if(dialog != null) {
			dialog.dismiss();
			dialog = null;
		}

		return this;
	}

	public Context getContext() {
		return context;
	}

	public Window getWindow() {
		if(dialog != null) {
			return dialog.getWindow();
		}

		return null;
	}

	public interface OnButtonClickListener {
		void clicked(DialogBuilder dialog);
	}
}