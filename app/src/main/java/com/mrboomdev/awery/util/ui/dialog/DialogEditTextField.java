package com.mrboomdev.awery.util.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mrboomdev.awery.databinding.WidgetEdittextOutlinedBinding;

public class DialogEditTextField implements DialogField {
	private int imeFlags = EditorInfo.IME_FLAG_NO_FULLSCREEN;
	private int type = EditorInfo.TYPE_CLASS_TEXT;
	private Runnable completionCallback;
	private Context context;
	private String hint, error, text;
	private int lines;
	private TextInputLayout view;
	private TextInputEditText editText;

	public DialogEditTextField(@NonNull Context context, @StringRes int hint) {
		this.hint = ContextCompat.getString(context, hint);
		this.context = context;
	}

	public DialogEditTextField(Context context, String hint) {
		this.hint = hint;
		this.context = context;
	}

	public DialogEditTextField(Context context) {
		this.context = context;
	}

	public void setError(@StringRes int res) {
		setError(context.getString(res));
	}

	public void setError(@Nullable String error) {
		this.error = error;

		if(view != null) {
			view.setError(error);
		}
	}

	public void setLinesCount(int lines) {
		this.lines = lines;

		if(editText != null) {
			editText.setLines(lines);
		}
	}

	public void setText(Object text) {
		if(text == null) this.text = "";
		else this.text = text.toString();

		if(editText != null) {
			editText.setText(this.text);
		}
	}

	public void setCompletionCallback(Runnable callback) {
		this.completionCallback = callback;

		if(editText != null) editText.setOnEditorActionListener((v, actionId, event) -> switch(actionId) {
			case EditorInfo.IME_ACTION_DONE,
					EditorInfo.IME_ACTION_GO,
					EditorInfo.IME_ACTION_SEARCH,
					EditorInfo.IME_ACTION_SEND -> {
				if(callback == null) {
					yield false;
				}

				callback.run();
				yield true;
			}

			default -> false;
		});
	}

	/**
	 * Sets provided flags to the wrapped EditText.
	 * Note that the IME_FLAG_NO_FULLSCREEN is already being applied by default.
	 * @param flags EditorInfo.IME_FLAG_* bit masked values
	 */
	public void setImeFlags(int flags) {
		this.imeFlags = EditorInfo.IME_FLAG_NO_FULLSCREEN | flags;
		if(view != null) editText.setImeOptions(this.imeFlags);
	}

	/**
	 * Sets provided flags to the wrapped EditText.
	 * @param flags EditorInfo.TYPE_* bit masked values
	 */
	public void setType(int flags) {
		this.type = flags;
		if(view != null) editText.setInputType(type);
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

		var binding = WidgetEdittextOutlinedBinding.inflate(
				LayoutInflater.from(context),
				null, false);

		view = binding.getRoot();
		editText = binding.edittext;
		editText.setHint(hint);

		// We run all setters for a case if those values
		// were set before view was created.
		setText(text);
		setError(error);
		setType(type);
		setImeFlags(imeFlags);
		setLinesCount(lines);
		setCompletionCallback(completionCallback);

		return view;
	}

	@Override
	public void deAttach() {
		this.view = null;
		this.editText = null;
		this.context = null;
	}
}