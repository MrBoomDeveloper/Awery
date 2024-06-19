package com.mrboomdev.awery.util.ui.fields;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mrboomdev.awery.databinding.WidgetEdittextOutlinedBinding;
import com.mrboomdev.awery.sdk.util.Callbacks;

public class EditTextField extends FancyField<TextInputLayout> {
	private final Context context;
	private TextInputEditText editText;
	private Callbacks.Callback1<String> editListener;
	private Runnable completionListener;
	private String hint, error, text;
	private int imeFlags = EditorInfo.IME_FLAG_NO_FULLSCREEN;
	private int type = EditorInfo.TYPE_CLASS_TEXT;
	private int lines;

	public EditTextField(@NonNull Context context, @StringRes int hint) {
		this.hint = ContextCompat.getString(context, hint);
		this.context = context;
	}

	public EditTextField(Context context, String hint) {
		this.hint = hint;
		this.context = context;
	}

	public EditTextField(Context context) {
		this.context = context;
	}

	public void setHint(String hint) {
		this.hint = hint;

		if(isCreated()) {
			editText.setHint(hint);
		}
	}

	public void setError(@StringRes int res) {
		setError(context.getString(res));
	}

	public void setError(@Nullable String error) {
		this.error = error;

		if(isCreated()) {
			getView().setError(error);
		}
	}

	public void setEditListener(Callbacks.Callback1<String> editListener) {
		this.editListener = editListener;

		if(isCreated() && editListener != null) {
			editText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					editListener.run(s.toString());
				}

				@Override
				public void afterTextChanged(Editable s) {}
			});
		}
	}

	public void setLinesCount(int lines) {
		this.lines = lines;

		if(isCreated()) {
			editText.setLines(lines);
		}
	}

	public void setText(Object text) {
		if(text == null) this.text = "";
		else this.text = text.toString();

		if(isCreated()) {
			editText.setText(this.text);
		}
	}

	public void setCompletionListener(Runnable callback) {
		this.completionListener = callback;
		if(!isCreated()) return;

		editText.setOnEditorActionListener((v, actionId, event) -> switch(actionId) {
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
		if(isCreated()) editText.setImeOptions(this.imeFlags);
	}

	/**
	 * Sets provided flags to the wrapped EditText.
	 * @param flags EditorInfo.TYPE_* bit masked values
	 */
	public void setType(int flags) {
		this.type = flags;

		if(isCreated()) {
			editText.setInputType(type);
		}
	}

	public String getText() {
		if(!isCreated()) {
			return "";
		}

		var text = editText.getText();
		return text != null ? text.toString() : "";
	}

	@Override
	public TextInputLayout createView() {
		var binding = WidgetEdittextOutlinedBinding.inflate(LayoutInflater.from(context));
		editText = binding.edittext;

		// We run all setters for a case if those values
		// were set before view was created.
		setHint(hint);
		setText(text);
		setError(error);
		setType(type);
		setImeFlags(imeFlags);
		setLinesCount(lines);
		setCompletionListener(completionListener);
		setEditListener(editListener);

		return binding.getRoot();
	}
}