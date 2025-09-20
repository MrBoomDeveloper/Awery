package androidx.preference

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
class PreferenceScreen {
	private val prefs = mutableListOf<Preference>()
	
	fun getPreferenceCount(): Int {
		return prefs.size
	}
	
	fun getPreference(index: Int): Preference {
		return prefs[index]
	}
	
	@PlatformSdk
	fun addPreference(preference: Preference): Boolean {
		return prefs.add(preference)
	}
	
	@PlatformSdk
	fun getContext(): Context {
		throw RuntimeException("Stub!")
	}
}