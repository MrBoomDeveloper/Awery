package mihonx.source.utils

import com.mrboomdev.awery.SharedPreferences
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.utils.ExtensionSdk
import eu.kanade.tachiyomi.source.Source

@ExtensionSdk(since = "1.6")
fun Source.sourcePreferences(): SharedPreferences = sourcePreferences(id)

@ExtensionSdk(since = "1.6")
fun sourcePreferences(id: Long): SharedPreferences = Platform.getSharedPreferences(id.toString())