package androidx.preference /*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
abstract class TwoStatePreference(context: Context): Preference(context) {
	private var checked = false
	private var summaryOn: CharSequence? = null
	private var summaryOff: CharSequence? = null

	init {
		setDefaultValue(false)
	}
	
	@PlatformSdk
	fun setChecked(checked: Boolean) {
		this.checked = checked
	}
	
	@PlatformSdk
	fun isChecked(): Boolean {
		return checked
	}
	
	@PlatformSdk
	fun getSummaryOn(): CharSequence? {
		return summaryOn
	}
	
	@PlatformSdk
	fun setSummaryOn(summary: CharSequence?) {
		this.summaryOn = summary
	}

	@PlatformSdk
	fun getSummaryOff(): CharSequence? {
		return summaryOff
	}

	@PlatformSdk
	fun setSummaryOff(summary: CharSequence?) {
		this.summaryOff = summary
	}
	
	override fun getSummary(): CharSequence? {
		val summary = super.getSummary()
		if(summary != null) {
			return summary
		}

		val checked = isChecked()
		if(checked && this.summaryOn != null) {
			return this.summaryOn
		} else if(!checked && this.summaryOff != null) {
			return this.summaryOff
		}

		return null
	}
	
	@PlatformSdk
	fun getDisableDependentsState() {
		throw RuntimeException("Stub!")
	}

	@PlatformSdk
	fun setDisableDependentsState(value: Boolean) {
		throw RuntimeException("Stub!")
	}
}