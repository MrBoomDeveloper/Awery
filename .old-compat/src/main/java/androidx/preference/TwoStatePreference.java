package androidx.preference;

import android.content.Context;

public abstract class TwoStatePreference {
	public boolean isChecked;
	
	public TwoStatePreference(Context context) {}
	
	public boolean isChecked() {
		return isChecked;
	}
	
	public void setChecked(boolean checked) {
		this.isChecked = checked;
	}
	
	public CharSequence getSummaryOn() {
		throw new UnsupportedOperationException();
	}
	
	public void setSummaryOn(CharSequence summary) {
		throw new UnsupportedOperationException();
	}
	
	public CharSequence getSummaryOff() {
		throw new UnsupportedOperationException();
	}
	
	public void setSummaryOff(CharSequence summary) {
		throw new UnsupportedOperationException();
	}
	
	public boolean getDisableDependentsState() {
		throw new UnsupportedOperationException();
	}
	
	public void setDisableDependentsState(boolean disableDependentsState) {
		throw new UnsupportedOperationException();
	}
}
