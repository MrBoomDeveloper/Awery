package androidx.preference

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
class MultiSelectListPreference(
	context: Context
): DialogPreference(context) {
	private var entries: Array<CharSequence>? = null
	private var entryValues: Array<CharSequence>? = null

	@PlatformSdk
	fun setEntries(entries: Array<CharSequence>?) {
		this.entries = entries
	}

	@PlatformSdk
	fun getEntries(): Array<CharSequence>? {
		return entries
	}

	@PlatformSdk
	fun setEntryValues(entryValues: Array<CharSequence>?) {
		this.entryValues = entryValues
	}

	@PlatformSdk
	fun getEntryValues(): Array<CharSequence>? {
		return entryValues
	}

	@PlatformSdk
	fun setValues(values: Set<String>?) {
		throw RuntimeException("Stub!")
	}

	@PlatformSdk
	fun getValues(): Set<String>? {
		throw RuntimeException("Stub!")
	}

	@PlatformSdk
	fun findIndexOfValue(value: String?): Int {
		throw RuntimeException("Stub!")
	}
}