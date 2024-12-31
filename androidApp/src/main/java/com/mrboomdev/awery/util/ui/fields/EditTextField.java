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

public class EditTextField extends FancyField<TextInputLayout> {
	private final Context context;
	private TextInputLayout layout;
	private TextInputEditText editText;
	private EditListener editListener;
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

		if(editText != null) {
			editText.setHint(hint);
		}
	}

	public void setError(@StringRes int res) {
		setError(context.getString(res));
	}

	public void setError(@Nullable String error) {
		this.error = error;

		if(layout != null) {
			layout.setError(error);
		}
	}

	public void setEditListener(EditListener editListener) {
		this.editListener = editListener;

		if(editText != null && editListener != null) {
			editText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					editListener.onEditText(s.toString());
				}

				@Override
				public void afterTextChanged(Editable s) {}
			});
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

	public void setCompletionListener(Runnable callback) {
		this.completionListener = callback;
		if(editText == null) return;

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
		if(editText != null) editText.setImeOptions(this.imeFlags);
	}

	/**
	 * Sets provided flags to the wrapped EditText.
	 * @param flags EditorInfo.TYPE_* bit masked values
	 */
	public void setType(int flags) {
		this.type = flags;

		if(editText != null) {
			editText.setInputType(type);
		}
	}

	public String getText() {
		if(editText == null) {
			return "";
		}

		var text = editText.getText();
		return text != null ? text.toString() : "";
	}

	@Override
	public TextInputLayout createView() {
		var binding = WidgetEdittextOutlinedBinding.inflate(LayoutInflater.from(context));
		layout = binding.getRoot();
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
	
	public interface EditListener {
		void onEditText(String text);
	}
}