package com.mrboomdev.awery.util.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ani.awery.R;

public class DialogBuilder {
	private final Map<Integer, Field> fieldMap = new HashMap<>();
	private final List<Field> fields = new ArrayList<>();
	private OnButtonClickListener okListener, cancelListener;
	private final Context context;
	private String title, ok, cancel;
	private Dialog dialog;

	public DialogBuilder(Context context) {
		this.context = context;
	}

	public DialogBuilder setTitle(String title) {
		this.title = title;
		return this;
	}

	public DialogBuilder setPositiveButton(String ok, OnButtonClickListener listener) {
		this.okListener = listener;
		this.ok = ok;
		return this;
	}

	public DialogBuilder setCancelButton(String cancel, OnButtonClickListener listener) {
		this.cancelListener = listener;
		this.cancel = cancel;
		return this;
	}

	public DialogBuilder addField(int id, Field field) {
		fieldMap.put(id, field);
		fields.add(field);
		return this;
	}

	public DialogBuilder addInputField(int id, String hint) {
		addField(id, new InputField(context, id, hint));
		return this;
	}

	public <T extends Field> T getField(int id, @NonNull Class<T> clazz) {
		return clazz.cast(getField(id));
	}

	@SuppressWarnings("unchecked")
	public <T extends Field> T getField(int id) {
		return (T) fieldMap.get(id);
	}

	public DialogBuilder create() {
		if(dialog != null) {
			dismiss();
		}

		var linear = new LinearLayoutCompat(context);
		linear.setOrientation(LinearLayoutCompat.VERTICAL);
		linear.setBackgroundResource(R.drawable.dialog_background);

		if(title != null) {
			var text = new AppCompatTextView(context);
			text.setText(title);
			linear.addView(text);
		}

		dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(linear);
		return this;
	}

	public DialogBuilder show() {
		if(dialog == null) {
			create();
		}

		dialog.show();
		return this;
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

	public static class InputField implements Field {
		private final Context context;
		private final int id;
		private String hint, error;
		private TextInputLayout view;
		private TextInputEditText editText;

		public InputField(Context context, int id, String hint) {
			this.hint = hint;
			this.id = id;
			this.context = context;
		}

		public void setError(String error) {
			this.error = error;

			if(view != null) {
				view.setError(error);
			}
		}

		public int getId() {
			return id;
		}

		public String getText() {
			if(editText != null) {
				var text = editText.getText();

				if(text != null) {
					return text.toString();
				}
			}
			return "";
		}

		@Override
		public View getView() {
			if(view != null) {
				return view;
			}

			view = new TextInputLayout(context);

			editText = new TextInputEditText(context);
			editText.setHint(hint);
			view.addView(editText);

			setError(error);

			return view;
		}

		@Override
		public void deAttach() {
			view = null;
			editText = null;
		}
	}

	public interface Field {
		View getView();
		void deAttach();
	}

	public interface OnButtonClickListener {
		void clicked(DialogBuilder dialog);
	}
}