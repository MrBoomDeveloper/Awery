package androidx.preference /*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context
import android.widget.EditText
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
class EditTextPreference(context: Context): DialogPreference(context) {
	private var text: String? = null
	private var onBindEditTextListener: OnBindEditTextListener? = null
	
	@PlatformSdk
	fun getText(): String? {
		return text
	}

	@PlatformSdk
	fun setText(text: String?) {
		this.text = text
	}

	@PlatformSdk
	fun getOnBindEditTextListener(): OnBindEditTextListener? {
		return onBindEditTextListener
	}

	@PlatformSdk
	fun setOnBindEditTextListener(onBindEditTextListener: OnBindEditTextListener?) {
		this.onBindEditTextListener = onBindEditTextListener
	}

	@PlatformSdk
	interface OnBindEditTextListener {
		@PlatformSdk
		fun onBindEditText(editText: EditText)
	}
}