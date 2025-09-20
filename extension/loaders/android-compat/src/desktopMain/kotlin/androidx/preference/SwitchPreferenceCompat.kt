package androidx.preference

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
class SwitchPreferenceCompat(
	context: Context
): TwoStatePreference(context)