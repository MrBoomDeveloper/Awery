package androidx.preference;

import android.content.Context;

import java.util.Set;

public class MultiSelectListPreference extends DialogPreference {
	public CharSequence[] entries, entryValues;
	public Set<String> values;
	
	public MultiSelectListPreference(Context context) {}
	
	public void setEntries(CharSequence[] entries) {
		this.entries = entries;
	}
	
	public CharSequence[] getEntries() {
		return entries;
	}
	
	public void setEntryValues(CharSequence[] entryValues) {
		this.entryValues = entryValues;
	}
	
	public CharSequence[] getEntryValues() {
		return entryValues;
	}
	
	public void setValues(Set<String> values) {
		this.values = values;
	}
	
	public Set<String> getValues() {
		return values;
	}
	
	public int findIndexOfValue(String values) {
		throw new UnsupportedOperationException();
	} 
}
