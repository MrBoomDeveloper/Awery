package androidx.preference;

public class Preference {
	public OnPreferenceChangeListener onPreferenceChangeListener;
	public OnPreferenceClickListener onPreferenceClickListener;
	public Object defaultValue;
	public String key;
	public CharSequence title, summary;
	public boolean isEnabled, isVisible;
	
	public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
		this.onPreferenceChangeListener = onPreferenceChangeListener;
	}
	
	public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
		this.onPreferenceClickListener = onPreferenceClickListener;
	}
	
	public CharSequence getTitle() {
		return title;
	}
	
	public void setTitle(CharSequence title) {
		this.title = title;
	}
	
	public CharSequence getSummary() {
		return summary;
	}
	
	public void setSummary(CharSequence summary) {
		this.summary = summary;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public interface OnPreferenceChangeListener {
		boolean onPreferenceChange(Preference preference, Object newValue);
	}
	
	public interface OnPreferenceClickListener {
		boolean onPreferenceClick(Preference preference);
	}
}
