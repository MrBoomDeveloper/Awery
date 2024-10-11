package com.mrboomdev.awery.util.ui.dialog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.mrboomdev.awery.app.App.resolveAttr;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.spPx;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

@SuppressWarnings("unchecked")
public abstract class BaseDialogBuilder<T extends BaseDialogBuilder<?>> {
	private final Context context;
	private final List<View> fields = new LinkedList<>();
	private final WeakHashMap<View, Object> addedFields = new WeakHashMap<>();
	private ScrollView scroller;
	private ViewGroup fieldsWrapper;
	private Dialog dialog;
	private Callback1<T> dismissListener;
	private OnButtonClickListener<T> okListener, cancelListener, neutralListener;
	private String title, message, okButtonLabel, cancelButtonLabel, neutralButtonLabel;
	private boolean isCancelable = true, didCreateRoot;

	public BaseDialogBuilder(Context context) {
		this.context = context;
	}

	public T setTitle(String title) {
		this.title = title;
		return (T) this;
	}

	public T setTitle(@StringRes int res) {
		return setTitle(context.getString(res));
	}

	public T setMessage(String message) {
		this.message = message;
		return (T) this;
	}

	public T setMessage(@StringRes int res) {
		return setMessage(context.getString(res));
	}

	public T setPositiveButton(String label, OnButtonClickListener<T> listener) {
		this.okListener = listener;
		this.okButtonLabel = label;
		return (T) this;
	}
	
	public interface Callback1<T> {
		void run(T arg);
	}

	public T setPositiveButton(@StringRes int label, OnButtonClickListener<T> listener) {
		return setPositiveButton(context.getString(label), listener);
	}

	public T setNeutralButton(String label, OnButtonClickListener<T> listener) {
		this.neutralListener = listener;
		this.neutralButtonLabel = label;
		return (T) this;
	}

	public T setNeutralButton(@StringRes int label, OnButtonClickListener<T> listener) {
		return setNeutralButton(context.getString(label), listener);
	}

	public T setCancelable(boolean isCancelable) {
		this.isCancelable = isCancelable;
		return (T) this;
	}

	public void performPositiveClick() {
		if(okListener != null) {
			okListener.clicked((T) this);
		}
	}

	public void performNeutralClick() {
		if(neutralListener != null) {
			neutralListener.clicked((T) this);
		}
	}

	public void performNegativeClick() {
		if(cancelListener != null) {
			cancelListener.clicked((T) this);
		}
	}

	public T setNegativeButton(String label, OnButtonClickListener<T> listener) {
		this.cancelListener = listener;
		this.cancelButtonLabel = label;
		return (T) this;
	}

	public T setNegativeButton(@StringRes int label, OnButtonClickListener<T> listener) {
		return setNegativeButton(context.getString(label), listener);
	}

	public T setOnDismissListener(Callback1<T> listener) {
		this.dismissListener = listener;
		return (T) this;
	}

	public T addView(View field, int index) {
		fields.add(index, field);

		if(fieldsWrapper != null) {
			addField(field, index);
		}

		return (T) this;
	}

	private void addField(View field, int index) {
		if(addedFields.get(field) != null) {
			return;
		}

		fieldsWrapper.addView(field, index);
		addedFields.put(field, new Object());
	}

	public T addView(View view) {
		addView(view, fields.size());
		return (T) this;
	}
	
	public interface Result1<T, A> {
		T run(A arg);
	}

	public T addView(@NonNull Result1<View, ViewGroup> callback) {
		createRoot();
		addView(callback.run(fieldsWrapper));
		return (T) this;
	}

	@Nullable
	protected abstract View getContentView(View parentView);

	private void createRoot() {
		if(didCreateRoot) return;
		didCreateRoot = true;

		scroller = new ScrollView(context);
		scroller.setVerticalScrollBarEnabled(false);

		var padding = (int) resolveAttr(getContext(), android.R.attr.dialogPreferredPadding)
				.getDimension(getContext().getResources().getDisplayMetrics());

		var fieldsLinear = new LinearLayoutCompat(context);
		setHorizontalPadding(fieldsLinear, padding);
		fieldsLinear.setOrientation(LinearLayoutCompat.VERTICAL);
		scroller.addView(fieldsLinear, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
		fieldsWrapper = fieldsLinear;
	}

	public void destroy() {
		if(scroller != null) {
			scroller.removeAllViews();
			scroller = null;
		}

		fieldsWrapper = null;
	}

	public T create() {
		if(dialog != null) {
			dismiss();
		}

		createRoot();

		var builder = new MaterialAlertDialogBuilder(context);
		builder.setCancelable(isCancelable);

		if(title != null) {
			var titleView = new MaterialTextView(getContext());
			titleView.setTextSize(spPx(titleView, 20));
			titleView.setText(title);
			titleView.setTextColor(resolveAttrColor(getContext(), com.google.android.material.R.attr.colorOnBackground));
			builder.setCustomTitle(titleView);

			var paddingTop = dpPx(titleView, 18);

			var padding = (int) resolveAttr(getContext(), android.R.attr.dialogPreferredPadding)
					.getDimension(getContext().getResources().getDisplayMetrics());

			titleView.setPadding(padding, paddingTop, padding, 0);
		}

		if(message == null) {
			setTopPadding(scroller, dpPx(scroller, 8));
		}

		var contentView = getContentView(fieldsWrapper);

		if(contentView != null) {
			fieldsWrapper.addView(contentView);
		}

		for(var field : fields) {
			addField(field, -1);
		}

		if(fieldsWrapper.getChildCount() > 0) {
			builder.setView(scroller);
		}

		if(title != null) builder.setTitle(title);
		if(message != null) builder.setMessage(message);

		if(okButtonLabel != null) builder.setPositiveButton(okButtonLabel,
				(dialog, which) -> okListener.clicked((T) this));

		if(cancelButtonLabel != null) builder.setNegativeButton(cancelButtonLabel,
				(dialog, which) -> cancelListener.clicked((T) this));

		if(neutralButtonLabel != null) builder.setNeutralButton(neutralButtonLabel,
				(dialog, which) -> neutralListener.clicked((T) this));

		if(dismissListener != null) builder.setOnDismissListener(
				v -> dismissListener.run((T) this));

		var alertDialog = builder.show();
		dialog = alertDialog;

		if(okButtonLabel != null) alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
				.setOnClickListener(v -> okListener.clicked((T) this));

		if(cancelButtonLabel != null) alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
				.setOnClickListener(v -> cancelListener.clicked((T) this));

		if(neutralButtonLabel != null) alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
				.setOnClickListener(v -> neutralListener.clicked((T) this));

		return (T) this;
	}

	public T show() {
		if(dialog == null) {
			create();
		}

		dialog.show();
		return (T) this;
	}

	public boolean isShown() {
		return dialog != null && dialog.isShowing();
	}

	public T dismiss() {
		if(dialog != null) {
			dialog.dismiss();
			runOnUiThread(this::destroy);
			dialog = null;
		}

		return (T) this;
	}

	public Context getContext() {
		return context;
	}

	public Dialog getDialog() {
		return dialog;
	}

	public Window getWindow() {
		if(getDialog() != null) {
			return getDialog().getWindow();
		}

		return null;
	}

	public interface OnButtonClickListener<T> {
		void clicked(T dialog);
	}
}