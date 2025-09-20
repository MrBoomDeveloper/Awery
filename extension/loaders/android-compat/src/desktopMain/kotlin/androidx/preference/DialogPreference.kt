package androidx.preference

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
abstract class DialogPreference(context: Context): Preference(context) {
	private var dialogTitle: CharSequence? = null
	private var dialogMessage: CharSequence? = null

	@PlatformSdk
	fun getDialogTitle(): CharSequence? {
		return dialogTitle
	}

	@PlatformSdk
	fun setDialogTitle(dialogTitle: CharSequence?) {
		this.dialogTitle = dialogTitle
	}

	@PlatformSdk
	fun getDialogMessage(): CharSequence? {
		return dialogMessage
	}

	@PlatformSdk
	fun setDialogMessage(dialogMessage: CharSequence?) {
		this.dialogMessage = dialogMessage
	}
}