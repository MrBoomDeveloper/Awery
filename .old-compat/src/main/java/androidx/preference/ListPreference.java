package androidx.preference;

import android.content.Context;

public class ListPreference extends Preference {
	private CharSequence[] entries, entryValues;
	
	public ListPreference(Context context) {}
	
	public CharSequence[] getEntries() {
		return entries;
	}
	
	public void setEntries(CharSequence[] entries) {
		this.entries = entries;
	}
	
	public int findIndexOfValue(String value) {
		throw new UnsupportedOperationException();
	}
	
	public CharSequence[] getEntryValues() {
		return entryValues;
	}
	
	public void setEntryValues(CharSequence[] entryValues) {
		this.entryValues = entryValues;
	}
	
	public void setValueIndex(int index) {
		throw new UnsupportedOperationException();
	}
	
	public String getValue() {
		throw new UnsupportedOperationException();
	}
	
	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}
}
