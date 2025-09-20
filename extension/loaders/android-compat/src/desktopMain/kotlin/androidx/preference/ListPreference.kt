package androidx.preference

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
class ListPreference(context: Context): Preference(context) {
	private var entries: Array<CharSequence>? = null
	private var entryValues: Array<CharSequence>? = null

	@PlatformSdk
	fun getEntries(): Array<CharSequence>? {
		return entries
	}

	@PlatformSdk
	fun setEntries(entries: Array<CharSequence>?) {
		this.entries = entries
	}

	@PlatformSdk
	fun findIndexOfValue(value: String?): Int {
		if(value != null && entryValues != null) {
			for(i in entryValues!!.indices.reversed()) {
				// TODO: Uncomment once TextUtils implemented
//				if(TextUtils.equals(entryValues!![i].toString(), value)) {
//					return i
//				}
			}
		}
		return -1
	}

	@PlatformSdk
	fun getEntryValues(): Array<CharSequence>? {
		return entryValues
	}

	@PlatformSdk
	fun setEntryValues(entryValues: Array<CharSequence>?) {
		this.entryValues = entryValues
	}

	@PlatformSdk
	fun setValueIndex(index: Int) {
		throw RuntimeException("Stub!")
	}

	@PlatformSdk
	fun getValue(): String? {
		throw RuntimeException("Stub!")
	}

	@PlatformSdk
	fun setValue(value: String?) {
		throw RuntimeException("Stub!")
	}
}