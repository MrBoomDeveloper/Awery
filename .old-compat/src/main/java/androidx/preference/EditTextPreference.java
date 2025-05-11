package androidx.preference;

import android.content.Context;
import android.widget.EditText;

public class EditTextPreference extends DialogPreference {
	public OnBindEditTextListener onBindEditTextListener;
	public String text;
	
	public EditTextPreference(Context context) {}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setOnBindEditTextListener(OnBindEditTextListener onBindEditTextListener) {
		this.onBindEditTextListener = onBindEditTextListener;
	}
	
	OnBindEditTextListener getOnBindEditTextListener() {
		return onBindEditTextListener;
	}
	
	public interface OnBindEditTextListener {
		void onBindEditText(EditText editText);
	}
}
