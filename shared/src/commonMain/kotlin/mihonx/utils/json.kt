package mihonx.utils

import com.mrboomdev.awery.utils.ExtensionSdk
import kotlinx.serialization.json.Json

/**
 * @since extensions-lib 1.6
 */
@ExtensionSdk
val defaultJson: Json = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
}