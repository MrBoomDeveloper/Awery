package androidx.preference

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
open class Preference(context: Context) {
	private var key: String? = null
	private var defaultValue: Any? = null
	private var title: CharSequence? = null
	private var summary: CharSequence? = null
	private var isVisible = true

	@PlatformSdk
	fun setEnabled(enabled: Boolean) {
		throw RuntimeException("Stub!")
	}

	fun isEnabled() {
		throw RuntimeException("Stub!")
	}

	@PlatformSdk
	fun setKey(key: String) {
		this.key = key
	}

	@PlatformSdk
	fun getKey(): String? {
		return key
	}

	@PlatformSdk
	fun setTitle(title: CharSequence?) {
		this.title = title
	}

	@PlatformSdk
	fun getTitle(): CharSequence? {
		return title
	}

	@PlatformSdk
	fun setSummary(summary: CharSequence?) {
		this.summary = summary
	}

	@PlatformSdk
	open fun getSummary(): CharSequence? {
		return summary
	}

	@PlatformSdk
	fun setVisible(visible: Boolean) {
		isVisible = visible
	}
	
	fun isVisible(): Boolean {
		return isVisible
	}

	@PlatformSdk
	fun setDefaultValue(defaultValue: Any?) {
		this.defaultValue = defaultValue
	}
	
	fun callChangeListener(newValue: Any?): Boolean {
		return false
	}

	@PlatformSdk
	fun setOnPreferenceChangeListener(onPreferenceChangeListener: OnPreferenceChangeListener?) {}

	@PlatformSdk
	fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener?) {
		throw RuntimeException("Stub!")
	}

	@PlatformSdk
	interface OnPreferenceClickListener {
		@PlatformSdk
		fun onPreferenceClick(preference: Preference?): Boolean
	}

	@PlatformSdk
	interface OnPreferenceChangeListener {
		@PlatformSdk
		fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean
	}
}