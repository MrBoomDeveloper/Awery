package androidx.preference;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class PreferenceScreen {
	public List<Preference> preferences = new ArrayList<>();
	
	public boolean addPreference(Preference preference) {
		return preferences.add(preference);
	}
	
	public Context getContext() {
		throw new UnsupportedOperationException();
	}
}