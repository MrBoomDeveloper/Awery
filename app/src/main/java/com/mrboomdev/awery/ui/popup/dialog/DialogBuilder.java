package com.mrboomdev.awery.ui.popup.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mrboomdev.awery.util.Callbacks;
import com.mrboomdev.awery.util.ui.ViewUtil;
import com.mrboomdev.awery.util.ui.dialog.DialogCustomField;
import com.mrboomdev.awery.util.ui.dialog.DialogField;

import java.util.LinkedList;
import java.util.List;

public class DialogBuilder {
	private final List<DialogField> fields = new LinkedList<>();
	private Callbacks.Callback1<DialogBuilder> dismissListener;
	private OnButtonClickListener okListener, cancelListener, neutralListener;
	private final Context context;
	private ViewGroup fieldsWrapper;
	private String title, message, okButtonLabel, cancelButtonLabel, neutralButtonLabel;
	private Dialog dialog;

	public DialogBuilder(Context context) {
		this.context = context;
	}

	public DialogBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	public DialogBuilder setMessage(String message) {
		this.message = message;
		return this;
	}

	public DialogBuilder setPositiveButton(String ok, OnButtonClickListener listener) {
		this.okListener = listener;
		this.okButtonLabel = ok;
		return this;
	}

	public DialogBuilder setNeutralButton(String neutral, OnButtonClickListener listener) {
		this.neutralListener = listener;
		this.neutralButtonLabel = neutral;
		return this;
	}

	public void performOkClick() {
		if(okListener != null) {
			okListener.clicked(this);
		}
	}

	public void performNegativeClick() {
		if(neutralListener != null) {
			neutralListener.clicked(this);
		}
	}

	public void performCancelClick() {
		if(cancelListener != null) {
			cancelListener.clicked(this);
		}
	}

	public DialogBuilder setCancelButton(String cancel, OnButtonClickListener listener) {
		this.cancelListener = listener;
		this.cancelButtonLabel = cancel;
		return this;
	}

	public DialogBuilder setOnDismissListener(Callbacks.Callback1<DialogBuilder> listener) {
		this.dismissListener = listener;
		return this;
	}

	public DialogBuilder addView(View view, int index) {
		addField(new DialogCustomField(view), index);
		return this;
	}

	public DialogBuilder addView(View view) {
		addView(view, fields.size());
		return this;
	}

	public DialogBuilder addField(DialogField field, int index) {
		fields.add(index, field);

		if(fieldsWrapper != null) {
			fieldsWrapper.addView(field.getView(), index);
		}

		return this;
	}

	public DialogBuilder addField(DialogField field) {
		addField(field, fields.size());
		return this;
	}

	public DialogBuilder create() {
		if(dialog != null) {
			dismiss();
		}

		/*var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);
		linear.setBackgroundResource(R.drawable.dialog_background);*/

		if(title != null) {
			/*var text = new AppCompatTextView(context);
			text.setTextSize(ViewUtil.spPx(18));
			text.setText(title);

			ViewUtil.setPadding(text, ViewUtil.dpPx(16));
			linear.addView(text);*/
		}

		var scroller = new ScrollView(context);
		ViewUtil.setTopPadding(scroller, ViewUtil.dpPx(8));
		//linear.addView(scroller);

		var fieldsLinear = new LinearLayoutCompat(context);
		ViewUtil.setHorizontalPadding(fieldsLinear, ViewUtil.dpPx(24));
		fieldsLinear.setOrientation(LinearLayoutCompat.VERTICAL);
		scroller.addView(fieldsLinear, new ViewGroup.LayoutParams(ViewUtil.MATCH_PARENT, ViewUtil.WRAP_CONTENT));
		fieldsWrapper = fieldsLinear;

		for(var field : fields) {
			fieldsLinear.addView(field.getView());
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
		builder.setView(scroller);

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

			for(var field : fields) {
				field.deAttach();
			}

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